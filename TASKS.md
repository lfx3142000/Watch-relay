# ChatGPT Watch Relay — TASKS.md

## Project status

**Project:** ChatGPT Watch Relay  
**Repository:** `lfx3142000/Watch-relay`  
**Goal:** Build an Android phone app that automatically detects when ChatGPT finishes responding, sends the readable response to a Samsung Galaxy Watch, and lets the user send preset or voice/custom replies from the watch back into the same ChatGPT conversation.

**Current status:** Initial Android shell builds in CI. Notification relay, watch actions, inline reply receiver, manual share fallback, basic Accessibility monitoring, and a first-pass Accessibility command sender are in place. The sender queues preset/custom watch replies, opens ChatGPT, attempts to focus the message box, paste/set command text, and tap a detected Send control. It includes retry/timeout handling and posts a failure notification if the command cannot be sent. The app records and displays basic diagnostics for package, captured text, likely response text, input candidates, send candidates, and command status. A first-pass `ChatGptScreenReader` now filters common UI chrome and uses a tail-of-conversation heuristic to extract likely latest response text instead of sending all visible screen text. Recent user testing indicates that sending can work, but multi-line or overly long preset command text may prevent the Accessibility sender from finding or enabling the ChatGPT Send button. Short one-line command text is now the top reliability priority.

**Critical MVP decision:** The MVP must include an automatic relay loop. Manual share/copy is allowed only as a fallback/debug path, not as the primary MVP.

---

## MVP definition

The MVP is successful only when this end-to-end loop works on an Android phone paired with a Galaxy Watch:

```text
1. User starts or continues a ChatGPT conversation on the phone.
2. ChatGPT generates a response.
3. Relay app automatically detects that generation has completed.
4. Relay app automatically extracts the latest ChatGPT response text.
5. Relay app sends a watch-readable notification with the first response chunk.
6. Galaxy Watch displays the response notification.
7. User can tap More to read additional chunks.
8. User can tap short one-line preset actions such as Continue, Status, Did you finish?, Send link, or Stop monitoring.
9. User can send a custom reply through notification inline reply when supported.
10. Relay app sends the selected preset command or custom reply back into the active ChatGPT conversation.
11. Relay app returns to monitoring for the next response.
```

Manual import/share/copy features are useful fallback features, but they do not satisfy the MVP by themselves.

---

## Important constraints and realities

- No OpenAI API use in the MVP.
- No backend server.
- No API keys.
- The app must work through the existing ChatGPT app or ChatGPT web UI.
- Automatic response detection and reply sending require Android Accessibility permission.
- Watch integration is through Android notifications mirrored to Galaxy Watch.
- Phone should be assumed unlocked for MVP automation reliability.
- Full lock-screen operation is a future enhancement, not MVP.
- UI automation may break if ChatGPT changes its app/web layout.
- The app must always include a fallback mode when automation fails.
- Preset command text must be very short and one line only. Multi-line response/command text can make the ChatGPT compose field taller or shift UI elements, causing the Accessibility sender to miss the Send button.

---

## Current top reliability priority

Shorten all preset watch commands before additional deep tuning.

Use these short command strings unless testing shows a better option:

- `Continue`
- `Status`
- `Did you finish?`
- `Send link`
- `Stop monitoring`

Implementation notes:

- Do not insert multi-line preset command text into ChatGPT.
- Do not add explanatory text to preset commands.
- Keep custom/voice replies as user-entered text, but preset buttons should stay one-line.
- After inserting text, verify the ChatGPT Send button is visible/enabled before tapping.
- If the Send button is not found, record diagnostics that include command length, whether the command contained newline characters, input candidate count, and send candidate count.

---

# Phase 0 — Repository setup

## Status

Mostly complete. CI build passed after JVM target fix.

## Tasks

- [x] Create GitHub repository.
- [x] Add initial `TASKS.md`.
- [x] Revise `TASKS.md` so MVP requires automatic ChatGPT detection, watch notification, and watch reply functionality.
- [x] Add initial `README.md`.
- [x] Add `.gitignore` for Android/Kotlin project.
- [x] Create initial Android project shell.
- [x] Commit initial project shell.
- [ ] Confirm project opens in Android Studio.
- [ ] Confirm app builds locally.
- [x] Confirm app builds in GitHub Actions CI. Run `26391318581` passed.
- [x] Document normal Android build workflow in README.

## Acceptance criteria

- [x] Repository exists.
- [x] `TASKS.md` is committed.
- [x] Android app skeleton files exist.
- [x] Android app skeleton builds in CI.

---

# Phase 1 — Android app shell and required permissions

## Status

In progress.

## Completed files

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/chatgpt_accessibility_service.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/java/com/example/chatgptwatchrelay/MainActivity.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/accessibility/ChatGptAccessibilityService.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/accessibility/ChatGptCommandSender.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/accessibility/ChatGptScreenReader.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/notifications/NotificationHelper.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/notifications/CommandReceiver.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/notifications/ReplyReceiver.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/Chunker.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/CommandRepository.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/RelayDiagnostics.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/relay/RelayState.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/launch/ChatGptLauncher.kt`
- `app/src/main/java/com/example/chatgptwatchrelay/fallback/ShareReceiverActivity.kt`

## Tasks

- [x] Create Kotlin Android project structure.
- [ ] Configure Jetpack Compose. Current shell uses native Android views to reduce setup complexity.
- [x] Set minSdk to 26 or higher.
- [x] Add app name: `ChatGPT Watch Relay`.
- [x] Add notification permission handling for Android 13+.
- [x] Add Accessibility Service declaration in manifest.
- [x] Add Accessibility Service configuration XML.
- [x] Add basic onboarding controls explaining/accessing Accessibility settings.
- [x] Add button to open Android Accessibility settings.
- [ ] Add status indicator: Accessibility enabled/disabled.
- [ ] Add status indicator: notification permission enabled/disabled.
- [x] Add button: `Send Test Watch Notification`.
- [x] Add button: `Open ChatGPT`.
- [x] Add button: `Start Monitoring`.
- [x] Add button: `Stop Monitoring`.
- [x] Add diagnostics text view and refresh button.
- [ ] Add persistent local storage for app settings. Current `RelayState` is in-memory only.

---

# Phase 2 — Accessibility monitoring foundation

## Status

Started.

## Tasks

- [x] Create `ChatGptAccessibilityService.kt`.
- [x] Detect foreground app/package at a basic level.
- [x] Identify when ChatGPT app is active using package name contains `openai`.
- [x] Identify when Chrome/browser target is active using package name contains `chrome` as a basic fallback.
- [x] Capture accessibility node tree text while ChatGPT/browser is visible.
- [x] Add relay monitoring state: active/inactive.
- [x] Add pending-command handling before response monitoring.
- [x] Add debug screen/log showing detected package, screen text count, likely response count, and candidate input/send nodes.
- [ ] Add safe throttling so the service does not over-scan. Current approach uses event-driven updates and simple stability count only.
- [ ] Add error state if ChatGPT screen is not detectable.
- [ ] Add privacy warning that captured screen text is processed locally.

---

# Phase 3 — ChatGPT generation state detection

## Status

Started, but still crude.

## Tasks

- [ ] Create dedicated `ChatGptUiStateDetector.kt`.
- [ ] Define formal states: `UNKNOWN`, `IDLE`, `GENERATING`, `COMPLETE_CANDIDATE`, `COMPLETE_CONFIRMED`, `ERROR`.
- [x] Track changes in likely latest response text over time in the Accessibility Service.
- [ ] Detect likely generating state explicitly.
- [x] Detect likely completed state after likely response text is stable for several accessibility events.
- [ ] Make stability delay configurable.
- [x] Avoid sending duplicate notifications for the same likely response using a response fingerprint.
- [x] Store a fingerprint/hash of the last notified response.
- [x] Add debug display of current detected state through diagnostics summary.

---

# Phase 4 — Latest response extraction

## Status

Started. Current implementation is heuristic and needs real-device tuning.

## Tasks

- [x] Create `ChatGptScreenReader.kt`.
- [x] Identify likely assistant response text from accessibility node tree using UI-line filtering and tail-of-conversation heuristic.
- [ ] Prefer the most recent assistant message using stronger role/message-boundary detection.
- [x] Exclude common navigation/sidebar/button text where possible.
- [x] Exclude common input box text where possible.
- [x] Normalize whitespace.
- [x] Store latest full likely response locally in memory.
- [x] Store response fingerprint.
- [x] Add debug view of extracted response preview via diagnostics/likely response counts.
- [ ] Add fallback extraction strategy for Chrome web UI after real accessibility data is observed.
- [x] Add fallback manual share path for failures.

---

# Phase 5 — Automatic watch notification with chunks

## Status

Started.

## Tasks

- [x] Create `Chunker.kt`.
- [x] Default chunk size: 700–900 characters.
- [x] Preserve word boundaries when possible.
- [x] Store `lastFullResponse`.
- [x] Store `currentChunkIndex`.
- [x] Store `totalChunks` via chunk list size.
- [ ] Store timestamp.
- [x] Create notification channel: `ChatGPT Relay`.
- [x] Create notification title format: `ChatGPT done — 1/4`.
- [x] Create notification body with current chunk.
- [x] Add notification action: `More`.
- [x] Add notification action: `Continue`.
- [x] Add notification action: `Status`.
- [x] Add notification action: `Did you finish?`.
- [x] Add notification action: `Send link`.
- [x] Add notification action: `Stop monitoring`.
- [x] Add notification action: `Reply` using RemoteInput.
- [x] Add final chunk state: `End of response`.
- [x] Add fallback for short responses.

---

# Phase 6 — Preset command actions from watch

## Status

Implemented first-pass auto-send path; needs short-command cleanup, real-device testing, and better diagnostics.

## Tasks

- [x] Create `CommandRepository.kt`.
- [x] Define default preset commands.
- [ ] Replace any long or multi-line preset commands with short one-line commands: `Continue`, `Status`, `Did you finish?`, `Send link`, and `Stop monitoring`.
- [ ] Add guard/test to ensure preset command strings contain no newline characters.
- [ ] Add diagnostics for command length and newline presence when a send attempt fails.
- [x] Create `CommandReceiver.kt`.
- [x] Receive notification action taps.
- [x] Map action taps to command text.
- [x] Copy command text to clipboard as fallback.
- [x] Open or return to ChatGPT.
- [x] Create `ChatGptCommandSender.kt`.
- [x] Queue preset command for Accessibility send.
- [x] Use Accessibility to find and focus a likely ChatGPT message input.
- [x] Paste command into the message input or use `ACTION_SET_TEXT` fallback.
- [x] Find and tap a likely Send control.
- [x] Restart response monitoring after send.
- [x] Add retry and timeout handling for pending command sends.
- [x] Add failure notification if command could not be sent.
- [x] Add diagnostics for detected input/send candidates.
- [ ] Real-device test: Continue from watch sends into ChatGPT.
- [ ] Real-device test: Status from watch sends into ChatGPT.
- [ ] Real-device test: Did you finish? from watch sends into ChatGPT.
- [ ] Real-device test: Send link from watch sends into ChatGPT.
- [ ] Real-device test: Stop monitoring from watch stops monitoring without inserting text into ChatGPT unless intentionally designed otherwise.

---

# Phase 7 — Custom voice/text reply from watch

## Status

Implemented first-pass auto-send path; needs real-device testing.

## Tasks

- [x] Create `ReplyReceiver.kt`.
- [x] Add notification inline reply action.
- [x] Capture RemoteInput reply text.
- [x] Queue custom reply for Accessibility send.
- [x] Copy reply to clipboard as fallback.
- [x] Open or return to ChatGPT.
- [x] Use Accessibility to find and focus message input.
- [x] Paste reply text or use `ACTION_SET_TEXT` fallback.
- [x] Tap likely Send control.
- [x] Restart monitoring after send.
- [x] Add basic user-visible error if inline reply is unavailable.
- [x] Add retry/timeout fallback through shared command sender.
- [ ] Real-device test: watch shows a reply option.
- [ ] Real-device test: dictated reply is received and sent into ChatGPT.

---

# Current next step

Update `CommandRepository.kt` and notification action labels so preset watch commands are short one-line strings only: `Continue`, `Status`, `Did you finish?`, `Send link`, and `Stop monitoring`. Build on `main`, install as an update, and test whether short commands allow the Accessibility sender to keep finding/enabling the ChatGPT Send button reliably.