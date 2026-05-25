# ChatGPT Watch Relay — TASKS.md

## Project status

**Project:** ChatGPT Watch Relay  
**Repository:** `lfx3142000/Watch-relay`  
**Goal:** Build an Android phone app that relays ChatGPT responses to a Samsung Galaxy Watch and lets the user respond with preset commands or voice/custom replies without using OpenAI API tokens.

**Current status:** Planning complete. Repo exists. Initial task file added.

**Core decision:** Build this as an Android phone app first. The Galaxy Watch will receive normal Android notifications mirrored from the phone. A dedicated Wear OS app can be considered later, but it is not needed for the MVP.

---

## Product concept

ChatGPT Watch Relay is a local-first Android app. It does not call the OpenAI API. Instead, it helps move content between an existing ChatGPT conversation and a watch-friendly notification/reply interface.

Primary workflow:

```text
ChatGPT response on phone
→ user shares/copies response to ChatGPT Watch Relay
→ relay app chunks the response
→ relay app posts watch-readable notification
→ Galaxy Watch shows response chunk
→ user taps More / Continue / Summarize / Shorter / Open
→ relay app copies a preset command and opens ChatGPT
→ optional later: relay app auto-pastes/sends command using Accessibility Service
```

---

## MVP definition

The first useful version is successful when:

```text
1. ChatGPT gives a response.
2. User selects/copies/shares the response to ChatGPT Watch Relay.
3. Galaxy Watch shows the first response chunk.
4. User can tap More, Continue, Summarize, Shorter, or Open.
5. App copies the correct command and opens ChatGPT.
6. User manually pastes/sends the command, or a later automation does it.
```

No OpenAI API, account login, backend, or server is required for the MVP.

---

## Technical choices

- Platform: Android phone app
- Language: Kotlin
- UI: Jetpack Compose
- Minimum SDK: 26+
- Storage: DataStore or SharedPreferences
- Backend: none
- API keys: none
- OpenAI API: not used
- Watch integration: Android notification mirroring to Galaxy Watch
- Optional automation: Android Accessibility Service in a later phase

---

## Recommended project structure

```text
Watch-relay/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/chatgptwatchrelay/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ShareReceiverActivity.kt
│   │   │   ├── NotificationHelper.kt
│   │   │   ├── CommandRepository.kt
│   │   │   ├── Chunker.kt
│   │   │   ├── RelayState.kt
│   │   │   ├── CommandReceiver.kt
│   │   │   ├── ReplyReceiver.kt
│   │   │   ├── ChatGptLauncher.kt
│   │   │   └── accessibility/
│   │   │       └── ChatGptAccessibilityService.kt
│   │   └── res/
│   ├── build.gradle.kts
├── settings.gradle.kts
├── README.md
└── TASKS.md
```

---

# Phase 0 — Repository setup

## Status

In progress.

## Tasks

- [x] Create GitHub repository.
- [x] Add initial `TASKS.md`.
- [ ] Add initial `README.md`.
- [ ] Add `.gitignore` for Android/Kotlin project.
- [ ] Create initial Android Studio project.
- [ ] Commit initial project shell.
- [ ] Confirm project opens in Android Studio.
- [ ] Confirm app builds locally.

## Acceptance criteria

- [x] Repository exists.
- [x] `TASKS.md` is committed.
- [ ] Android app skeleton builds.

---

# Phase 1 — Basic Android app shell

## Status

Not started.

## Goal

Create a working Android app shell called **ChatGPT Watch Relay**.

## Tasks

- [ ] Create Kotlin Android project.
- [ ] Configure Jetpack Compose.
- [ ] Set minSdk to 26 or higher.
- [ ] Add main screen.
- [ ] Add app title and basic layout.
- [ ] Add notification permission handling for Android 13+.
- [ ] Add button: `Paste from Clipboard`.
- [ ] Add button: `Send Test Notification`.
- [ ] Add button: `Open ChatGPT`.
- [ ] Add preview area for last captured response.
- [ ] Add basic local storage for last response.

## Main screen draft

```text
ChatGPT Watch Relay

[Paste from Clipboard]
[Send Test Notification]
[Open ChatGPT]

Last captured response:
Preview text...

Commands:
- Continue
- Summarize
- Shorter
- Explain simply
- Next steps
```

## Acceptance criteria

- [ ] App installs on Android phone.
- [ ] App opens without crashing.
- [ ] Test notification appears on phone.
- [ ] Test notification mirrors to Galaxy Watch when watch notifications are enabled.
- [ ] Clipboard text can be pasted into app and shown in preview.

---

# Phase 2 — Android share target

## Status

Not started.

## Goal

Allow ChatGPT response text to be sent to the relay app using Android's share menu.

## Tasks

- [ ] Add `ACTION_SEND` intent filter for `text/plain`.
- [ ] Create `ShareReceiverActivity`.
- [ ] Extract shared text from incoming intent.
- [ ] Save shared text as latest response.
- [ ] Open app preview screen after sharing.
- [ ] Trigger first watch notification automatically after share.
- [ ] Handle empty or unsupported share payloads gracefully.

## Expected flow

```text
Select ChatGPT response text
→ Share
→ ChatGPT Watch Relay
→ App receives text
→ App chunks text
→ App sends first notification chunk
```

## Acceptance criteria

- [ ] App appears in Android share sheet.
- [ ] Shared text is captured correctly.
- [ ] Captured text appears in app.
- [ ] First chunk is posted as notification.

---

# Phase 3 — Chunked watch notifications

## Status

Not started.

## Goal

Make ChatGPT responses readable on a Galaxy Watch by splitting long responses into chunks.

## Tasks

- [ ] Create `Chunker.kt`.
- [ ] Default chunk size: 700–900 characters.
- [ ] Preserve word boundaries when possible.
- [ ] Store `lastFullResponse`.
- [ ] Store `currentChunkIndex`.
- [ ] Store `totalChunks`.
- [ ] Store `timestamp`.
- [ ] Create notification title format: `ChatGPT 1/4`.
- [ ] Create notification body with current chunk.
- [ ] Add `More` action.
- [ ] Add end-of-response state.
- [ ] Add fallback for short responses.

## Notification draft

```text
ChatGPT 1/4

Here is the issue: the Gradle build is failing because the Kotlin plugin version...

Actions:
More | Summarize | Continue | Open
```

## Acceptance criteria

- [ ] Long response is split into multiple chunks.
- [ ] `More` advances to the next chunk.
- [ ] Final chunk clearly indicates end of response.
- [ ] Notification is readable on the watch.

---

# Phase 4 — Preset command actions

## Status

Not started.

## Goal

Let watch notification buttons generate useful ChatGPT commands.

## Default preset commands

```text
Continue:
Continue from where you left off.

Summarize:
Summarize your last response in 5 short bullets.

Shorter:
Make that shorter and more direct.

Next Steps:
Give me the next concrete steps.

Explain Simply:
Explain that more simply.

Fix:
Propose the exact fix and show the file changes.

Open:
Open the current ChatGPT conversation.
```

## Tasks

- [ ] Create `CommandRepository.kt`.
- [ ] Define default preset commands.
- [ ] Add notification action: `More`.
- [ ] Add notification action: `Continue`.
- [ ] Add notification action: `Summarize`.
- [ ] Add notification action: `Shorter`.
- [ ] Add notification action: `Open`.
- [ ] Create `CommandReceiver.kt`.
- [ ] On command tap, copy selected command to clipboard.
- [ ] Show confirmation notification or toast.
- [ ] Open ChatGPT app or web page.
- [ ] Add fallback if ChatGPT app is not installed.

## Acceptance criteria

- [ ] Tapping `Continue` copies the continue command.
- [ ] Tapping `Summarize` copies the summarize command.
- [ ] Tapping `Shorter` copies the shorter command.
- [ ] App opens ChatGPT after copying command.
- [ ] User can manually paste/send the command into ChatGPT.

---

# Phase 5 — Existing ChatGPT conversation support

## Status

Not started.

## Goal

Open the same ChatGPT conversation as consistently as possible.

## Tasks

- [ ] Add settings screen.
- [ ] Add setting: `ChatGPT open target`.
- [ ] Support target: ChatGPT app.
- [ ] Support target: ChatGPT web.
- [ ] Support target: custom conversation URL.
- [ ] Add field for saved conversation URL.
- [ ] Save URL locally.
- [ ] Use URL when opening ChatGPT.
- [ ] Add validation for `https://chatgpt.com/` URLs.
- [ ] Add fallback to general ChatGPT page if no URL is saved.

## Acceptance criteria

- [ ] User can save a ChatGPT conversation URL.
- [ ] `Open ChatGPT` opens that URL.
- [ ] Command actions open the selected target.
- [ ] Existing chat workflow is possible with a saved URL.

---

# Phase 6 — Persistent remote notification

## Status

Not started.

## Goal

Create an always-available watch control notification.

## Tasks

- [ ] Add toggle: `Enable persistent ChatGPT Remote`.
- [ ] Create persistent notification.
- [ ] Add button: `Continue`.
- [ ] Add button: `Summarize`.
- [ ] Add button: `Shorter`.
- [ ] Add button: `Open`.
- [ ] Add optional button: `Send Latest`.
- [ ] Let user dismiss or disable persistent notification.
- [ ] Ensure notification actions work from Galaxy Watch if mirrored.

## Acceptance criteria

- [ ] Persistent notification appears on phone.
- [ ] Notification appears on watch.
- [ ] Watch button taps trigger command actions.
- [ ] User can disable persistent notification.

---

# Phase 7 — Custom reply / voice reply support

## Status

Not started.

## Goal

Allow user to dictate or type a custom command from the watch.

## Approach

Use Android notification inline reply.

## Tasks

- [ ] Add notification `Reply` action.
- [ ] Create `ReplyReceiver.kt`.
- [ ] Capture inline reply text.
- [ ] Store last custom reply.
- [ ] Copy reply to clipboard.
- [ ] Open ChatGPT target.
- [ ] Later: send automatically using Accessibility Service.
- [ ] Add error handling if inline reply is not available on watch.

## Acceptance criteria

- [ ] Watch shows a reply option when supported.
- [ ] User can dictate or type custom reply.
- [ ] App receives the reply text.
- [ ] App opens ChatGPT with reply copied to clipboard.

---

# Phase 8 — Optional Accessibility automation

## Status

Not started.

## Goal

Automatically paste and send commands into ChatGPT.

## Warning

This phase is optional and fragile. It depends on screen layout, keyboard behavior, ChatGPT app/web UI, phone lock state, and Android accessibility permissions.

## Tasks

- [ ] Add Accessibility Service declaration.
- [ ] Create `ChatGptAccessibilityService.kt`.
- [ ] Add onboarding screen explaining permission.
- [ ] Add toggle: `Auto-paste command`.
- [ ] Add toggle: `Auto-send command`.
- [ ] Detect ChatGPT app or Chrome.
- [ ] Locate message input box.
- [ ] Paste command.
- [ ] Tap send.
- [ ] Add timeout and failure fallback.
- [ ] Add stop/cancel action.

## Acceptance criteria

- [ ] With phone unlocked, command can be pasted automatically.
- [ ] With auto-send enabled, command can be sent automatically.
- [ ] If automation fails, app falls back to clipboard/manual mode.
- [ ] User can disable accessibility automation.

---

# Phase 9 — Response capture improvements

## Status

Not started.

## Goal

Reduce manual copy/share steps.

## Tasks

- [ ] Add `Import Clipboard` button.
- [ ] Check if clipboard has text.
- [ ] Store clipboard text as latest response.
- [ ] Send first chunk to watch.
- [ ] When app opens and clipboard contains text, ask: `Send copied text to watch?`
- [ ] Avoid background clipboard polling due to Android restrictions.
- [ ] Consider optional floating button.
- [ ] Explore reading visible ChatGPT response text via Accessibility as experimental only.

## Acceptance criteria

- [ ] User can quickly import clipboard text.
- [ ] App does not behave unexpectedly with sensitive clipboard contents.
- [ ] User controls whether clipboard text is stored or sent.

---

# Phase 10 — Settings and customization

## Status

Not started.

## Goal

Let user customize commands and relay behavior.

## Tasks

- [ ] Add settings screen.
- [ ] Add default chunk size setting.
- [ ] Add command list editor.
- [ ] Add command label field.
- [ ] Add command prompt field.
- [ ] Add command reorder support.
- [ ] Add command visibility toggle.
- [ ] Add notification priority setting.
- [ ] Add clear last response button.
- [ ] Add clear clipboard after command option.
- [ ] Add export/import settings option if easy.

## Acceptance criteria

- [ ] User can edit preset commands.
- [ ] User can choose which commands appear in notification.
- [ ] User can adjust chunk size.
- [ ] User can clear stored response.

---

# Phase 11 — Polish, safety, and privacy

## Status

Not started.

## Goal

Make the app safe and clear to use.

## Tasks

- [ ] Add privacy note: app is local-only.
- [ ] Add explanation: no OpenAI API is used.
- [ ] Add explanation of notification mirroring.
- [ ] Add warning before Accessibility mode.
- [ ] Add `Clear all local data`.
- [ ] Avoid storing response history beyond latest response unless user enables it.
- [ ] Add basic error messages.
- [ ] Add empty state UI.
- [ ] Add app icon.
- [ ] Add README screenshots later.
- [ ] Add troubleshooting section.

## Acceptance criteria

- [ ] User understands what the app does and does not do.
- [ ] User can clear stored text.
- [ ] App has graceful error states.
- [ ] App is usable without Accessibility permission.

---

# Future ideas

- [ ] Dedicated Wear OS companion app.
- [ ] Telegram relay mode.
- [ ] GitHub Actions notification relay.
- [ ] `MORE`, `SUMMARY`, `CONTINUE` parser for incoming message replies.
- [ ] Android quick settings tile.
- [ ] App shortcut: `Send clipboard to watch`.
- [ ] Optional local history of recent relayed responses.
- [ ] Optional text-to-speech integration.
- [ ] Optional Tasker plugin compatibility.
- [ ] Optional GitHub workflow command bridge.

---

# AI continuation prompt

Use this prompt when asking an AI/code agent to continue the project:

```text
You are continuing the ChatGPT Watch Relay Android project. First read TASKS.md fully. Determine the next incomplete task in the current phase. Implement only the next small, logical set of changes. Keep the app local-first and do not add OpenAI API usage. Prefer a reliable manual relay before adding Accessibility automation. After making changes, update TASKS.md with completed items, current status, and any notes about files changed or remaining issues. If a task is blocked, add a clear blocker note and move to the next safe task.
```

---

# Current next step

Add `README.md`, `.gitignore`, and the initial Android Studio project shell.
