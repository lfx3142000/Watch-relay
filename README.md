# ChatGPT Watch Relay

Android relay app for sending completed ChatGPT responses to a Samsung Galaxy Watch and replying back into ChatGPT with preset or custom commands.

## MVP goal

The MVP is automation-first:

1. Detect an active ChatGPT screen using Android Accessibility.
2. Detect when the ChatGPT response appears complete.
3. Extract the latest response text locally.
4. Send the response to the watch as chunked Android notifications.
5. Allow watch actions such as **More**, **Continue**, **Summarize**, **Shorter**, **Open**, and inline **Reply**.
6. Send preset or custom commands back into ChatGPT using Accessibility automation.

## Constraints

- No OpenAI API.
- No backend server.
- No API keys.
- Local-first Android app.
- Accessibility automation is required for the automatic MVP loop.
- Manual share/clipboard relay is a fallback only.

## Current status

Initial Android project shell and relay plumbing are in progress. See [`TASKS.md`](TASKS.md) for the full build plan and status.

## Build check

A GitHub Actions workflow is present at `.github/workflows/android-build.yml` and is intended to run `gradle :app:assembleDebug --stacktrace`.
