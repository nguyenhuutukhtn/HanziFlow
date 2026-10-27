'use strict';
// Pure log-line formatter: formatLine(line) -> {kind, text} | null

const ERR_RE = /ASK-PM:|FAIL|Error|Traceback|KILLED/;

function str(v) {
  return typeof v === 'string' ? v : v == null ? '' : String(v);
}

function clip(s, n) {
  s = str(s).replace(/\s+/g, ' ').trim();
  return s.length > n ? s.slice(0, n) + '…' : s;
}

function summarize(obj) {
  const type = str(obj.type);
  let first = '';
  for (const k of Object.keys(obj)) {
    if (k !== 'type' && typeof obj[k] === 'string') { first = obj[k]; break; }
  }
  return clip([type, first].filter(Boolean).join(' ') || JSON.stringify(obj), 200);
}

function formatItem(ev, item) {
  const started = ev.type === 'item.started';
  switch (item.type) {
    case 'reasoning':
      return started ? null : { kind: 'think', text: '💭 ' + clip(item.text, 200) };
    case 'agent_message':
      return started ? null : { kind: 'say', text: '💬 ' + str(item.text).trim() };
    case 'command_execution': {
      const cmd = str(item.command);
      if (started) return { kind: 'cmd', text: '$ ' + cmd + ' …' };
      const code = item.exit_code == null ? '?' : item.exit_code;
      return { kind: 'cmd', text: '$ ' + cmd + ' → exit ' + code };
    }
    case 'file_change': {
      if (started) return null;
      const ch = Array.isArray(item.changes) ? item.changes : [];
      const parts = ch.map((c) => '✎ ' + str(c && c.path) + (c && c.kind ? ' (' + str(c.kind) + ')' : ''));
      return { kind: 'edit', text: parts.length ? parts.join('\n') : '✎ (file change)' };
    }
    default:
      return { kind: 'info', text: summarize(Object.assign({ type: ev.type + ':' + str(item.type) }, item.text ? { text: item.text } : {})) };
  }
}

function formatLine(line) {
  line = str(line).replace(/\r?\n$/, '');
  if (line.trim() === '') return null;
  let obj = null;
  if (line.trimStart()[0] === '{') {
    try { obj = JSON.parse(line); } catch (e) { obj = null; }
  }
  if (!obj || typeof obj !== 'object' || Array.isArray(obj)) {
    return { kind: ERR_RE.test(line) ? 'error' : 'raw', text: line };
  }
  try {
    switch (obj.type) {
      case 'thread.started':
      case 'turn.started':
        return null;
      case 'item.started':
      case 'item.completed':
        if (obj.item && typeof obj.item === 'object') return formatItem(obj, obj.item);
        return { kind: 'info', text: summarize(obj) };
      case 'turn.completed': {
        const u = obj.usage || {};
        const inp = u.input_tokens == null ? '?' : u.input_tokens;
        const out = u.output_tokens == null ? '?' : u.output_tokens;
        return { kind: 'info', text: 'turn done · tokens in ' + inp + ' / out ' + out };
      }
      case 'turn.failed':
      case 'error': {
        const m = (obj.error && (obj.error.message || obj.error)) || obj.message || '';
        return { kind: 'error', text: 'ERROR ' + clip(typeof m === 'string' ? m : JSON.stringify(m), 300) };
      }
      default:
        return { kind: 'info', text: summarize(obj) };
    }
  } catch (e) {
    return { kind: 'raw', text: line };
  }
}

module.exports = { formatLine };
