// Run with Node.js 22+. Set AI_API_KEY privately in the process environment.
async function main() {
  if (!process.env.AI_API_KEY) throw new Error('Set AI_API_KEY in your local environment first.');
  const response = await fetch('https://api.vilao.ai/v1/chat/completions', {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + process.env.AI_API_KEY, 'Content-Type': 'application/json' },
    body: JSON.stringify({ model: 'occ/claude-opus-4-8', messages: [{ role: 'user', content: 'Reply with exactly: Mentor connected' }], max_tokens: 32 }),
    signal: AbortSignal.timeout(60000)
  });
  if (!response.ok) throw new Error('ViLao returned HTTP ' + response.status);
  const data = await response.json();
  const answer = data.choices?.[0]?.message?.content;
  if (typeof answer !== 'string' || !answer.trim()) throw new Error('No answer in expected chat completion format.');
  console.log('ViLao response:', answer.slice(0, 200));
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
