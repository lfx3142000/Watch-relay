# ChatGPT Watch Relay — TASKS.md

## Project status

**Project:** ChatGPT Watch Relay  
**Repository:** `lfx3142000/Watch-relay`  
**Goal:** Build an Android phone app that automatically detects when ChatGPT finishes responding, sends the readable response to a Samsung Galaxy Watch, and lets the user send preset or voice/custom replies from the watch back into the same ChatGPT conversation.

**Current status:** Planning updated. Repo exists. MVP requirements revised to require automatic notification and reply functionality.

**Critical MVP decision:** The MVP must include an automatic relay loop. Manual share/copy is allowed only as a fallback/debug path, not as the primary MVP.

---

## Product concept

ChatGPT Watch Relay is a local-first Android relay app. It does not call the OpenAI API and does not use OpenAI API tokens. Instead, it uses Android notification mirroring, notification actions, inline replies, and Accessibility automation to bridge the existing ChatGPT app or ChatGPT web session with a Galaxy Watch.

Primary MVP workflow:

```text
User starts a ChatGPT response on phone
→ Relay app monitors the active ChatGPT screen using Accessibility
→ Relay app detects when ChatGPT stops generating
→ Relay app extracts the latest assistant response text
→ Relay app chunks the response into watch-readable sections
→ Relay app posts a notification mirrored to Galaxy Watch
→ User reads first chunk on watch
→ User taps More / Continue / Summarize / Shorter / Open / Reply
→ Relay app receives the watch action or dictated reply
→ Relay app opens/returns to the same ChatGPT conversation
→ Relay app pastes and sends the selected command or custom reply
→ Relay app monitors for the next ChatGPT response
```

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
8. User can tap preset actions such as Continue, Summarize, Shorter, or Open.
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

---

## Technical choices

- Platform: Android phone app first
- Language: Kotlin
- UI: Jetpack Compose
- Minimum SDK: 26+
- Storage: DataStore or SharedPreferences
- Backend: none
- API keys: none
- OpenAI API: not used
- Watch integration: Android notification mirroring to Galaxy Watch
- Automation: Android Accessibility Service required for MVP
- Notification actions: Android notification action buttons and RemoteInput inline reply
- Primary ChatGPT target: ChatGPT app if automation is reliable; Chrome/ChatGPT web as fallback target

---

## Recommended project structure

```text
Watch-relay/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/chatgptwatchrelay/
│   │   │   ├── MainActivity.kt
│   │   │   ├── onboarding/
│   │   │   │   └── PermissionOnboardingScreen.kt
│   │   │   ├── accessibility/
│   │   │   │   ├── ChatGptAccessibilityService.kt
│   │   │   │   ├── ChatGptScreenReader.kt
│   │   │   │   ├── ChatGptUiStateDetector.kt
│   │   │   │   └── ChatGptCommandSender.kt
│   │   │   ├── notifications/
│   │   │   │   ├── NotificationHelper.kt
│   │   │   │   ├── CommandReceiver.kt
│   │   │   │   └── ReplyReceiver.kt
│   │   │   ├── relay/
│   │   │   │   ├── RelayController.kt
│   │   │   │   ├── RelayState.kt
│   │   │   │   ├── Chunker.kt
│   │   │   │   ├── CommandRepository.kt
│   │   │   │   └── ResponseHistoryStore.kt
│   │   │   ├── launch/
│   │   │   │   └── ChatGptLauncher.kt
│   │   │   └── fallback/
│   │   │       └── ShareReceiverActivity.kt
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
- [x] Revise `TASKS.md` so MVP requires automatic ChatGPT detection, watch notification, and watch reply functionality.
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

# Phase 1 — Android app shell and required permissions

## Status

Not started.

## Goal

Create the Android app shell with the permissions and onboarding required for automatic relay operation.

## Tasks

- [ ] Create Kotlin Android project.
- [ ] Configure Jetpack Compose.
- [ ] Set minSdk to 26 or higher.
- [ ] Add app name: `ChatGPT Watch Relay`.
- [ ] Add notification permission handling for Android 13+.
- [ ] Add Accessibility Service declaration in manifest.
- [ ] Add Accessibility Service configuration XML.
- [ ] Add onboarding screen explaining why Accessibility is required.
- [ ] Add button to open Android Accessibility settings.
- [ ] Add status indicator: Accessibility enabled/disabled.
- [ ] Add status indicator: notification permission enabled/disabled.
- [ ] Add button: `Send Test Watch Notification`.
- [ ] Add button: `Open ChatGPT`.
- [ ] Add button: `Start Monitoring`.
- [ ] Add button: `Stop Monitoring`.
- [ ] Add local storage for app settings.

## Main screen draft

```text
ChatGPT Watch Relay

Status:
Accessibility: Off / On
Notifications: Off / On
Monitoring: Stopped / Active
Last response: None / Captured

[Enable Accessibility]
[Allow Notifications]
[Open ChatGPT]
[Start Monitoring]
[Send Test Watch Notification]

Preset commands:
Continue | Summarize | Shorter | Next Steps
```

## Acceptance criteria

- [ ] App installs on Android phone.
- [ ] App opens without crashing.
- [ ] User can enable notification permission.
- [ ] User can enable Accessibility Service.
- [ ] Test notification appears on phone.
- [ ] Test notification mirrors to Galaxy Watch when watch notifications are enabled.
- [ ] App can open ChatGPT app or ChatGPT web fallback.

---

# Phase 2 — Accessibility monitoring foundation

## Status

Not started.

## Goal

Build the foundation for automatically monitoring the ChatGPT UI.

## Tasks

- [ ] Create `ChatGptAccessibilityService.kt`.
- [ ] Detect foreground app/package.
- [ ] Identify when ChatGPT app is active.
- [ ] Identify when Chrome or browser ChatGPT page is active as fallback.
- [ ] Capture accessibility node tree snapshots while ChatGPT is visible.
- [ ] Add debug screen/log showing detected package, screen text count, and candidate input/send nodes.
- [ ] Add relay monitoring state: active/inactive.
- [ ] Add safe throttling so the service does not over-scan.
- [ ] Add error state if ChatGPT screen is not detectable.
- [ ] Add privacy warning that captured screen text is processed locally.

## Acceptance criteria

- [ ] App can detect when ChatGPT is the active screen.
- [ ] App can read accessible text nodes from ChatGPT screen.
- [ ] Debug screen shows whether ChatGPT UI is recognized.
- [ ] Monitoring can be started and stopped by the user.
- [ ] No network access is required.

---

# Phase 3 — ChatGPT generation state detection

## Status

Not started.

## Goal

Automatically detect when ChatGPT is generating and when the response has completed.

## MVP detection strategy

Use multiple signals instead of relying on one UI label:

- Send/stop button state changes.
- Presence or disappearance of stop-generating control.
- Accessibility text changes over time.
- Response text stability for a configured dwell time.
- Optional visible progress indicator detection if exposed through accessibility.

## Tasks

- [ ] Create `ChatGptUiStateDetector.kt`.
- [ ] Define states:
  - [ ] `UNKNOWN`
  - [ ] `IDLE`
  - [ ] `GENERATING`
  - [ ] `COMPLETE_CANDIDATE`
  - [ ] `COMPLETE_CONFIRMED`
  - [ ] `ERROR`
- [ ] Track changes in visible text over time.
- [ ] Detect likely generating state.
- [ ] Detect likely completed state after response text is stable for 2–5 seconds.
- [ ] Make stability delay configurable.
- [ ] Avoid sending duplicate notifications for the same response.
- [ ] Store a fingerprint/hash of the last notified response.
- [ ] Add debug display of current detected state.

## Acceptance criteria

- [ ] When ChatGPT starts generating, app enters `GENERATING`.
- [ ] When response stops changing, app enters `COMPLETE_CONFIRMED`.
- [ ] App does not notify repeatedly for the same response.
- [ ] App can recover if detection fails or screen changes.

---

# Phase 4 — Latest response extraction

## Status

Not started.

## Goal

Extract the latest assistant response text automatically after ChatGPT finishes.

## Tasks

- [ ] Create `ChatGptScreenReader.kt`.
- [ ] Identify likely assistant response text from accessibility node tree.
- [ ] Prefer the most recent assistant message.
- [ ] Exclude user prompt text where possible.
- [ ] Exclude input box text.
- [ ] Exclude navigation/sidebar text.
- [ ] Normalize whitespace.
- [ ] Store latest full response locally.
- [ ] Store timestamp and response fingerprint.
- [ ] Add debug view of extracted response preview.
- [ ] Add fallback extraction strategy for Chrome web UI.
- [ ] Add fallback manual import/share path for failures.

## Acceptance criteria

- [ ] After ChatGPT completes, the app extracts the latest response automatically.
- [ ] Extracted text is readable and does not mainly contain navigation/UI clutter.
- [ ] Latest response is saved locally.
- [ ] App can show extracted response preview on main screen.

---

# Phase 5 — Automatic watch notification with chunks

## Status

Not started.

## Goal

Automatically send the extracted ChatGPT response to the Galaxy Watch as a readable notification.

## Tasks

- [ ] Create `Chunker.kt`.
- [ ] Default chunk size: 700–900 characters.
- [ ] Preserve word boundaries when possible.
- [ ] Store `lastFullResponse`.
- [ ] Store `currentChunkIndex`.
- [ ] Store `totalChunks`.
- [ ] Store `timestamp`.
- [ ] Create notification channel: `ChatGPT Relay`.
- [ ] Create notification title format: `ChatGPT done — 1/4`.
- [ ] Create notification body with current chunk.
- [ ] Add notification action: `More`.
- [ ] Add notification action: `Continue`.
- [ ] Add notification action: `Summarize`.
- [ ] Add notification action: `Shorter`.
- [ ] Add notification action: `Open`.
- [ ] Add notification action: `Reply` using RemoteInput.
- [ ] Add final chunk state: `End of response`.
- [ ] Add fallback for short responses.

## Notification draft

```text
ChatGPT done — 1/4

Here is the issue: the Gradle build is failing because the Kotlin plugin version...

Actions:
More | Continue | Summarize | Reply
```

## Acceptance criteria

- [ ] Notification is posted automatically after ChatGPT completion.
- [ ] Notification appears on phone.
- [ ] Notification mirrors to Galaxy Watch.
- [ ] Watch can show the first chunk.
- [ ] `More` advances to the next chunk.
- [ ] Final chunk clearly indicates end of response.
- [ ] Duplicate notifications are avoided.

---

# Phase 6 — Preset command actions from watch

## Status

Not started.

## Goal

Let the user tap a preset reply on the watch and have that command sent back to the same ChatGPT conversation.

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
- [ ] Create `CommandReceiver.kt`.
- [ ] Receive notification action taps.
- [ ] Map action taps to command text.
- [ ] Copy command text to clipboard as fallback.
- [ ] Open or return to ChatGPT.
- [ ] Use Accessibility to focus the ChatGPT message input.
- [ ] Paste command into the message input.
- [ ] Tap send.
- [ ] Restart response monitoring after send.
- [ ] Add failure notification if command could not be sent.

## Acceptance criteria

- [ ] User can tap `Continue` on watch and app sends the command to ChatGPT.
- [ ] User can tap `Summarize` on watch and app sends the command to ChatGPT.
- [ ] User can tap `Shorter` on watch and app sends the command to ChatGPT.
- [ ] App returns to monitoring for the next ChatGPT response.
- [ ] If auto-send fails, command remains copied to clipboard and ChatGPT opens.

---

# Phase 7 — Custom voice/text reply from watch

## Status

Not started.

## Goal

Allow the user to dictate or type a custom command from the Galaxy Watch notification and send it back to ChatGPT.

## Approach

Use Android notification `RemoteInput`. On Galaxy Watch, this may allow voice dictation, keyboard input, or canned reply options depending on the watch/app notification behavior.

## Tasks

- [ ] Create `ReplyReceiver.kt`.
- [ ] Add notification inline reply action.
- [ ] Capture RemoteInput reply text.
- [ ] Store last custom reply.
- [ ] Copy reply to clipboard as fallback.
- [ ] Open or return to ChatGPT.
- [ ] Use Accessibility to focus message input.
- [ ] Paste reply text.
- [ ] Tap send.
- [ ] Restart monitoring after send.
- [ ] Add user-visible error if inline reply is unavailable.

## Acceptance criteria

- [ ] Watch shows a reply option when supported.
- [ ] User can dictate or type a custom reply.
- [ ] App receives the reply text.
- [ ] App sends the reply into ChatGPT automatically.
- [ ] App falls back to clipboard/open mode if auto-send fails.

---

# Phase 8 — ChatGPT target handling and same-chat support

## Status

Not started.

## Goal

Return commands to the same active ChatGPT conversation as reliably as possible.

## Tasks

- [ ] Create `ChatGptLauncher.kt`.
- [ ] Support target: ChatGPT app.
- [ ] Support target: ChatGPT web in Chrome.
- [ ] Support target: custom conversation URL.
- [ ] Add setting: `Preferred ChatGPT target`.
- [ ] Add setting: `Conversation URL`.
- [ ] Save URL locally.
- [ ] Validate `https://chatgpt.com/` URLs.
- [ ] Add fallback to general ChatGPT page.
- [ ] Detect whether target is already open and avoid unnecessary relaunch when possible.
- [ ] Prefer returning to current foreground ChatGPT session for MVP.

## Acceptance criteria

- [ ] User can select ChatGPT app or browser target.
- [ ] User can save a conversation URL.
- [ ] Command actions return to the intended ChatGPT conversation.
- [ ] App uses fallback open behavior when same-chat return is not possible.

---

# Phase 9 — Persistent remote notification

## Status

Not started.

## Goal

Create an always-available watch control notification for sending commands even when no response notification is active.

## Tasks

- [ ] Add toggle: `Enable persistent ChatGPT Remote`.
- [ ] Create persistent notification.
- [ ] Add button: `Continue`.
- [ ] Add button: `Summarize`.
- [ ] Add button: `Shorter`.
- [ ] Add button: `Open`.
- [ ] Add `Reply` RemoteInput action if supported.
- [ ] Let user dismiss or disable persistent notification.
- [ ] Ensure notification actions work from Galaxy Watch if mirrored.

## Acceptance criteria

- [ ] Persistent notification appears on phone.
- [ ] Notification appears on watch.
- [ ] Watch button taps send commands to ChatGPT.
- [ ] User can disable persistent notification.

---

# Phase 10 — Manual fallback paths

## Status

Not started.

## Goal

Provide fallback options for cases where Accessibility detection or extraction fails.

## Tasks

- [ ] Add `Import Clipboard` button.
- [ ] Add `Send Current Clipboard to Watch` action.
- [ ] Add Android share target for `text/plain`.
- [ ] Create `ShareReceiverActivity`.
- [ ] Extract shared text from incoming intent.
- [ ] Save shared text as latest response.
- [ ] Send first chunk to watch.
- [ ] Add error message explaining when manual fallback is needed.

## Acceptance criteria

- [ ] User can manually import clipboard text.
- [ ] User can share selected ChatGPT text to the relay app.
- [ ] Manual fallback sends watch notification using the same chunk/actions system.
- [ ] Manual fallback does not replace the automatic MVP loop.

---

# Phase 11 — Settings and customization

## Status

Not started.

## Goal

Let user customize command behavior, chunking, and automation reliability.

## Tasks

- [ ] Add settings screen.
- [ ] Add default chunk size setting.
- [ ] Add response stability delay setting.
- [ ] Add command list editor.
- [ ] Add command label field.
- [ ] Add command prompt field.
- [ ] Add command reorder support.
- [ ] Add command visibility toggle.
- [ ] Add notification priority setting.
- [ ] Add target app selection.
- [ ] Add clear last response button.
- [ ] Add clear clipboard after command option.
- [ ] Add automation diagnostics screen.

## Acceptance criteria

- [ ] User can edit preset commands.
- [ ] User can choose which commands appear in notification.
- [ ] User can adjust chunk size.
- [ ] User can tune detection stability delay.
- [ ] User can clear stored response.

---

# Phase 12 — Polish, safety, and privacy

## Status

Not started.

## Goal

Make the app safe, understandable, and reliable enough for daily use.

## Tasks

- [ ] Add privacy note: app is local-only.
- [ ] Add explanation: no OpenAI API is used.
- [ ] Add explanation of notification mirroring.
- [ ] Add warning before Accessibility mode.
- [ ] Add warning that ChatGPT UI automation may break after app/web updates.
- [ ] Add `Clear all local data`.
- [ ] Avoid storing response history beyond latest response unless user enables it.
- [ ] Add basic error messages.
- [ ] Add empty state UI.
- [ ] Add app icon.
- [ ] Add README screenshots later.
- [ ] Add troubleshooting section.

## Acceptance criteria

- [ ] User understands what the app does and does not do.
- [ ] User understands why Accessibility is required.
- [ ] User can clear stored text.
- [ ] App has graceful error states.
- [ ] App remains usable when automation fails.

---

# MVP build order

Build in this order:

```text
1. Android project shell
2. Notification permission + test watch notification
3. Accessibility Service declaration and onboarding
4. Detect ChatGPT foreground screen
5. Monitor ChatGPT visible text and generation state
6. Detect completed response automatically
7. Extract latest response automatically
8. Send chunked watch notification automatically
9. Add More action for chunks
10. Add preset command actions
11. Use Accessibility to paste/send preset commands into ChatGPT
12. Add RemoteInput custom reply from watch
13. Restart monitoring after command/reply send
14. Add manual clipboard/share fallback
15. Add settings/customization
```

---

# MVP acceptance test script

Use this script to validate the MVP:

```text
1. Install app on Android phone paired with Galaxy Watch.
2. Enable notification permission.
3. Enable ChatGPT Watch Relay Accessibility Service.
4. Enable watch notification mirroring for the app.
5. Open ChatGPT app or ChatGPT web.
6. Ask ChatGPT a prompt that takes at least several seconds.
7. Do not manually share/copy anything.
8. Wait for ChatGPT to finish.
9. Confirm Relay posts notification automatically.
10. Confirm Galaxy Watch receives the notification.
11. Tap More on watch and confirm next chunk appears.
12. Tap Summarize on watch.
13. Confirm phone opens/returns to ChatGPT.
14. Confirm app pastes and sends summarize command.
15. Wait for new ChatGPT response.
16. Confirm Relay detects and notifies again automatically.
17. Use watch inline reply to dictate a custom command.
18. Confirm command is sent into ChatGPT.
```

MVP is not complete until this script passes with no manual copy/share step.

---

# Future ideas

- [ ] Dedicated Wear OS companion app.
- [ ] Direct Wear OS app for local command buttons.
- [ ] Telegram relay mode.
- [ ] Google Messages relay mode.
- [ ] GitHub Actions notification relay.
- [ ] `MORE`, `SUMMARY`, `CONTINUE` parser for incoming message replies.
- [ ] Android quick settings tile.
- [ ] App shortcut: `Start ChatGPT monitoring`.
- [ ] Optional local history of recent relayed responses.
- [ ] Optional text-to-speech integration.
- [ ] Optional Tasker plugin compatibility.
- [ ] Optional GitHub workflow command bridge.

---

# AI continuation prompt

Use this prompt when asking an AI/code agent to continue the project:

```text
You are continuing the ChatGPT Watch Relay Android project. First read TASKS.md fully. The MVP must include automatic ChatGPT completion detection, automatic watch notification, and watch-to-ChatGPT reply sending. Manual share/copy is fallback only and does not satisfy MVP. Determine the next incomplete task in the current phase. Implement only the next small, logical set of changes. Keep the app local-first and do not add OpenAI API usage. Use Android Accessibility for ChatGPT screen monitoring and command sending. Use Android notifications, actions, and RemoteInput for Galaxy Watch interaction. After making changes, update TASKS.md with completed items, current status, files changed, and any blockers. If a task is blocked, add a clear blocker note and move to the next safe task.
```

---

# Current next step

Add `README.md`, `.gitignore`, and the initial Android Studio project shell, then begin Phase 1 permissions/onboarding.
