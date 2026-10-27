'use strict';
const test = require('node:test');
const assert = require('node:assert');
const { formatLine } = require('../format.js');

const j = (o) => JSON.stringify(o);

test('hidden events', () => {
  assert.strictEqual(formatLine(j({ type: 'thread.started', thread_id: 'x' })), null);
  assert.strictEqual(formatLine(j({ type: 'turn.started' })), null);
  assert.strictEqual(formatLine(''), null);
});

test('reasoning -> think, clipped to 200', () => {
  const r = formatLine(j({ type: 'item.completed', item: { type: 'reasoning', text: 'a'.repeat(500) } }));
  assert.strictEqual(r.kind, 'think');
  assert.ok(r.text.startsWith('💭 aaa'));
  assert.ok(r.text.length <= 205);
});

test('agent_message -> say', () => {
  const r = formatLine(j({ type: 'item.completed', item: { type: 'agent_message', text: 'done' } }));
  assert.deepStrictEqual(r, { kind: 'say', text: '💬 done' });
});

test('command_execution started/completed', () => {
  assert.deepStrictEqual(
    formatLine(j({ type: 'item.started', item: { type: 'command_execution', command: 'ls' } })),
    { kind: 'cmd', text: '$ ls …' });
  assert.deepStrictEqual(
    formatLine(j({ type: 'item.completed', item: { type: 'command_execution', command: 'ls', exit_code: 2 } })),
    { kind: 'cmd', text: '$ ls → exit 2' });
});

test('file_change -> edit', () => {
  const r = formatLine(j({ type: 'item.completed', item: { type: 'file_change', changes: [{ path: 'a.kt', kind: 'update' }, { path: 'b.kt', kind: 'add' }] } }));
  assert.strictEqual(r.kind, 'edit');
  assert.strictEqual(r.text, '✎ a.kt (update)\n✎ b.kt (add)');
});

test('turn.completed -> info with tokens', () => {
  const r = formatLine(j({ type: 'turn.completed', usage: { input_tokens: 10, output_tokens: 5 } }));
  assert.strictEqual(r.kind, 'info');
  assert.match(r.text, /in 10 \/ out 5/);
});

test('error and turn.failed -> error', () => {
  assert.strictEqual(formatLine(j({ type: 'error', message: 'boom' })).kind, 'error');
  const r = formatLine(j({ type: 'turn.failed', error: { message: 'nope' } }));
  assert.strictEqual(r.kind, 'error');
  assert.match(r.text, /nope/);
});

test('unknown JSON -> info summary, max 200', () => {
  const r = formatLine(j({ type: 'weird', note: 'hello', n: 1 }));
  assert.deepStrictEqual(r, { kind: 'info', text: 'weird hello' });
  assert.ok(formatLine(j({ type: 'x', s: 'b'.repeat(900) })).text.length <= 201);
});

test('missing fields never throw', () => {
  for (const o of [{ type: 'item.completed' }, { type: 'item.completed', item: {} }, { type: 'item.completed', item: { type: 'file_change' } }, { type: 'turn.completed' }, {}, { type: 'error' }]) {
    assert.doesNotThrow(() => formatLine(j(o)));
  }
});

test('malformed JSON -> raw', () => {
  assert.deepStrictEqual(formatLine('{"type": "item.comp'), { kind: 'raw', text: '{"type": "item.comp' });
});

test('plain and ASK-PM lines', () => {
  assert.deepStrictEqual(formatLine('hello world'), { kind: 'raw', text: 'hello world' });
  assert.strictEqual(formatLine('ASK-PM: which color?').kind, 'error');
  assert.strictEqual(formatLine('Traceback (most recent call last):').kind, 'error');
});
