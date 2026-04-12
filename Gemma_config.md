# Fix the context window (critical!)
Ollama sets the context window at 4096 by default. Even though Ollama says the context is larger, upon running the model 
it will use a default of 4k context — this has to be set up yourself. GitHub With agentic tools, 
4k fills up immediately and breaks tool calls.  

## Step 1 — Create a custom model with a larger context:

```bash
ollama run gemma4
>>> /set parameter num_ctx 32768
>>> /save gemma4-agent
>>> /bye
```

Or via Modelfile:

```bash
echo -e "FROM gemma4\nPARAMETER num_ctx 32768" > Modelfile
ollama create gemma4-agent -f Modelfile
```

## Step 2 — Configure OpenCode
Edit ~/.config/opencode/opencode.json:

```json
{
  "$schema": "https://opencode.ai/config.json",
  "provider": {
    "ollama": {
      "npm": "@ai-sdk/openai-compatible",
      "name": "Ollama (local)",
      "options": {
        "baseURL": "http://localhost:11434/v1"
      },
      "models": {
        "gemma4-agent": {
          "name": "gemma4-agent",
          "tools": true
        }
      }
    }
  }
}
```
OpenCode accesses local models through an OpenAI-compatible API endpoint at http://localhost:11434/v1.

## Step 3 — Run OpenCode
```bash
# Make sure Ollama is running first
ollama serve   # (usually already running as a daemon)

# Then launch OpenCode
opencode

# Or specify the model directly
opencode --model ollama/gemma4-agent
```

Troubleshooting tips
Tools not working / can't read files → almost always the context window issue from Step 2. Run ollama serve in a terminal and watch for truncation warnings.
Slow responses → on CPU, expect 1–3 tokens per second. The e2b model is the most practical option for CPU-only machines.

setting num_ctx not sticking → use the Modelfile approach instead of the interactive /set command, as it's more reliable.
Given that you've previously run vLLM on Apple Silicon for other projects, Ollama should feel very familiar. Gemma 4's native tool-calling support makes it actually viable for OpenCode's agentic file operations, unlike some other local models that only do chat.
