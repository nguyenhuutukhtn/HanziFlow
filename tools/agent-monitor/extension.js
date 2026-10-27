'use strict';
// Agent Monitor: opens a live panel for each run registered by scripts/agent_run.sh.
const fs = require('fs');
const path = require('path');
const { formatLine } = require('./format.js');

const STALE_MS = 10 * 60 * 1000;

function activate(context) {
  const vscode = require('vscode');
  const folder = (vscode.workspace.workspaceFolders || [])[0];
  if (!folder) return;
  const logsDir = path.join(folder.uri.fsPath, 'build', 'logs');
  const panels = new Map(); // id -> {panel, post, setRun, dispose}
  const startedAt = Date.now();

  function readRun(file) {
    try { return JSON.parse(fs.readFileSync(file, 'utf8')); } catch (e) { return null; }
  }

  function onRunFile(uri, initial) {
    const run = readRun(uri.fsPath);
    if (!run || !run.id) return;
    const existing = panels.get(run.id);
    if (existing) { existing.setRun(run); return; }
    if (run.status !== 'running') return;
    if (initial) {
      const age = startedAt - new Date(run.started).getTime();
      if (!(age < STALE_MS)) return;
    }
    openPanel(run, true);
  }

  function openPanel(run, preserveFocus) {
    const existing = panels.get(run.id);
    if (existing) { existing.panel.reveal(undefined, preserveFocus); existing.setRun(run); return; }
    const panel = vscode.window.createWebviewPanel(
      'agentMonitor', `agent ${run.id} · ${run.agent}`,
      { viewColumn: vscode.ViewColumn.Beside, preserveFocus },
      { enableScripts: true, retainContextWhenHidden: true });
    const nonce = require('crypto').randomBytes(16).toString('hex');
    panel.webview.html = html(nonce, panel.webview.cspSource);

    let current = run;
    let offset = 0;
    let rest = '';
    let watcher = null;
    let poll = null;
    let ready = false;
    const queue = [];
    const post = (m) => { if (ready) panel.webview.postMessage(m); else queue.push(m); };

    function readNew() {
      let fd;
      try {
        fd = fs.openSync(current.log, 'r');
        const size = fs.fstatSync(fd).size;
        if (size < offset) { offset = 0; rest = ''; }
        if (size === offset) return;
        const buf = Buffer.alloc(size - offset);
        fs.readSync(fd, buf, 0, buf.length, offset);
        offset = size;
        const parts = (rest + buf.toString('utf8')).split('\n');
        rest = parts.pop(); // partial last line waits for its newline
        const out = [];
        for (const l of parts) { const f = formatLine(l); if (f) out.push(f); }
        if (out.length) post({ type: 'lines', lines: out });
      } catch (e) { /* log not there yet */ }
      finally { if (fd !== undefined) try { fs.closeSync(fd); } catch (e) { /* ignore */ } }
    }

    function flushRest() {
      if (rest) { const f = formatLine(rest); rest = ''; if (f) post({ type: 'lines', lines: [f] }); }
    }

    function setRun(r) {
      current = r;
      post({ type: 'run', run: r });
      if (r.status !== 'running') { readNew(); flushRest(); }
    }

    function startTail() {
      try { watcher = fs.watch(current.log, () => readNew()); } catch (e) { watcher = null; }
      poll = setInterval(readNew, 1000); // fs.watch is unreliable on some filesystems
      readNew();
    }

    panel.webview.onDidReceiveMessage((m) => {
      if (m.type === 'ready') {
        ready = true;
        panel.webview.postMessage({ type: 'run', run: current });
        while (queue.length) panel.webview.postMessage(queue.shift());
      } else if (m.type === 'tell' && typeof m.text === 'string' && m.text.trim()) {
        const d = new Date();
        const hh = String(d.getHours()).padStart(2, '0');
        const mm = String(d.getMinutes()).padStart(2, '0');
        try {
          fs.mkdirSync(logsDir, { recursive: true });
          fs.appendFileSync(path.join(logsDir, `${current.id}.pm`), `${hh}:${mm} ${m.text.trim()}\n`);
          post({ type: 'lines', lines: [{ kind: 'pm', text: '📨 ' + m.text.trim() }] });
        } catch (e) {
          vscode.window.showErrorMessage('Agent Monitor: cannot write .pm file: ' + e.message);
        }
      }
    });

    const entry = {
      panel, setRun,
      dispose() { if (watcher) watcher.close(); if (poll) clearInterval(poll); },
    };
    panel.onDidDispose(() => { entry.dispose(); panels.delete(run.id); });
    panels.set(run.id, entry);
    startTail();
  }

  const pattern = new vscode.RelativePattern(folder, 'build/logs/*.run.json');
  const watcher = vscode.workspace.createFileSystemWatcher(pattern);
  watcher.onDidCreate((u) => onRunFile(u, false));
  watcher.onDidChange((u) => onRunFile(u, false));
  context.subscriptions.push(watcher, { dispose() { for (const p of panels.values()) p.dispose(); } });

  // Runs already in progress when the window opened (skip stale ones).
  try {
    for (const f of fs.readdirSync(logsDir)) {
      if (f.endsWith('.run.json')) onRunFile(vscode.Uri.file(path.join(logsDir, f)), true);
    }
  } catch (e) { /* no logs yet */ }

  context.subscriptions.push(vscode.commands.registerCommand('agentMonitor.openRun', async () => {
    let files = [];
    try { files = fs.readdirSync(logsDir).filter((f) => f.endsWith('.run.json')); } catch (e) { /* none */ }
    const items = files.map((f) => {
      const p = path.join(logsDir, f);
      const run = readRun(p);
      return run ? { label: run.id, description: `${run.agent} · ${run.status}`, detail: run.started, run, mtime: fs.statSync(p).mtimeMs } : null;
    }).filter(Boolean).sort((a, b) => b.mtime - a.mtime);
    if (!items.length) { vscode.window.showInformationMessage('No agent runs found in build/logs.'); return; }
    const pick = await vscode.window.showQuickPick(items, { placeHolder: 'Open agent run' });
    if (pick) openPanel(pick.run, false);
  }));
}

function html(nonce, cspSource) {
  return `<!DOCTYPE html>
<html><head><meta charset="utf-8">
<meta http-equiv="Content-Security-Policy" content="default-src 'none'; style-src ${cspSource} 'nonce-${nonce}'; script-src 'nonce-${nonce}';">
<style nonce="${nonce}">
  body { margin:0; padding:0; display:flex; flex-direction:column; height:100vh; font-family:var(--vscode-font-family); color:var(--vscode-foreground); background:var(--vscode-editor-background); }
  #head { padding:6px 10px; border-bottom:1px solid var(--vscode-panel-border); display:flex; gap:12px; align-items:center; flex-wrap:wrap; }
  #status { font-weight:bold; }
  #status.running { color:var(--vscode-charts-yellow, #cca700); }
  #status.ok { color:var(--vscode-charts-green, #388a34); }
  #status.bad { color:var(--vscode-charts-red, #f14c4c); }
  #log { flex:1; overflow-y:auto; padding:6px 10px; font-family:var(--vscode-editor-font-family); font-size:var(--vscode-editor-font-size); }
  .l { white-space:pre-wrap; word-break:break-word; margin:1px 0; }
  .think { opacity:.6; font-style:italic; }
  .say { color:var(--vscode-charts-blue, #3794ff); font-weight:bold; }
  .cmd { color:var(--vscode-terminal-ansiCyan, #11a8cd); }
  .edit { color:var(--vscode-charts-green, #388a34); }
  .info { opacity:.7; }
  .error { color:var(--vscode-errorForeground, #f14c4c); font-weight:bold; }
  .pm { color:var(--vscode-charts-purple, #b180d7); }
  #tell { padding:6px 10px; border-top:1px solid var(--vscode-panel-border); }
  #tell input { width:100%; box-sizing:border-box; padding:5px; color:var(--vscode-input-foreground); background:var(--vscode-input-background); border:1px solid var(--vscode-input-border, transparent); }
</style></head>
<body>
<div id="head"><span id="rid"></span><span id="agent"></span><span id="elapsed"></span><span id="status" class="running"></span></div>
<div id="log"></div>
<div id="tell"><input id="msg" placeholder="Nhắn PM" autocomplete="off"></div>
<script nonce="${nonce}">
  const vscode = acquireVsCodeApi();
  const log = document.getElementById('log');
  const $ = (id) => document.getElementById(id);
  let run = null, stick = true;
  log.addEventListener('scroll', () => { stick = log.scrollTop + log.clientHeight >= log.scrollHeight - 20; });
  function fmt(ms) { const s = Math.max(0, Math.floor(ms / 1000)); const m = Math.floor(s / 60); return m + 'm ' + String(s % 60).padStart(2, '0') + 's'; }
  function tick() {
    if (!run) return;
    const end = run.ended ? new Date(run.ended).getTime() : Date.now();
    $('elapsed').textContent = fmt(end - new Date(run.started).getTime());
  }
  setInterval(tick, 1000);
  function setRun(r) {
    run = r;
    $('rid').textContent = r.id; $('agent').textContent = r.agent;
    const st = $('status'); st.textContent = r.status;
    st.className = r.status === 'running' ? 'running' : r.status === 'exit 0' ? 'ok' : 'bad';
    tick();
  }
  window.addEventListener('message', (e) => {
    const m = e.data;
    if (m.type === 'run') setRun(m.run);
    else if (m.type === 'lines') {
      for (const l of m.lines) {
        const d = document.createElement('div');
        d.className = 'l ' + l.kind;
        d.textContent = l.text;
        log.appendChild(d);
      }
      if (stick) log.scrollTop = log.scrollHeight;
    }
  });
  $('msg').addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.isComposing && e.keyCode !== 229 && e.target.value.trim()) { vscode.postMessage({ type: 'tell', text: e.target.value }); e.target.value = ''; }
  });
  vscode.postMessage({ type: 'ready' });
</script></body></html>`;
}

function deactivate() {}

module.exports = { activate, deactivate };
