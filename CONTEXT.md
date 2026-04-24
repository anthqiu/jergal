# Jergal Context

## Product

Jergal is a retro game launcher for dual-screen Android handheld devices.

The interaction model references the Nintendo 3DS:

- The upper screen is primarily a showcase surface.
- The lower screen is primarily an interaction and browsing surface.
- Both screens must behave as one coordinated product, not as two unrelated Android activities.

## Device Model

- The `internal screen` is the top screen.
- The `external screen` is the bottom screen.
- The app should launch the top-screen activity on the internal display when possible.
- The app should launch the bottom-screen activity on the external display when possible.

## Design Direction

- The visual language should follow Material 3 Expressive.
- The implementation should remain restrained and product-like, not decorative.
- Avoid flashy backgrounds, ornamental typography, and presentation-heavy visual tricks.
- Prefer standard Material 3 structure: `Surface`, `Card`, `ListItem`, Material typography, Material shapes, and theme-driven color roles.
- Large-screen and dual-screen layouts should adapt structurally to available space rather than relying on purely visual effects.

## Screen Responsibilities

### Top Screen

- The top screen is a game showcase only.
- When the user highlights a game on the bottom screen, the top screen must update to show that game's hero presentation and logo.
- The top screen should not become the primary navigation surface.
- The top screen should behave like a launcher-owned immersive surface and may hide the system gesture bar and other system bars.
- The top screen should not be closable into another desktop by back-style root gestures.

### Bottom Screen

- The bottom screen is the primary interaction surface.
- It contains a horizontally scrolling `2 x N` game grid.
- Each game is represented by a Material 3 card.
- Each card contains only:
  - A square cover area
  - A title
- Cards should remain square.
- The cover should fill the full card area without internal black bars or inset framing.
- Card size should scale automatically with available space while keeping spacing consistent.
- Selecting or focusing a card must update the top screen immediately.

## App Drawer Behavior

- Swiping upward on the bottom screen should open the Android-style app drawer.
- The app drawer belongs only to the bottom-screen interaction model.
- The app drawer must open as a full-screen surface. There must be no half-expanded stage.
- During the open gesture, the top edge of the app drawer should follow the user's finger vertically.
- After the finger leaves the screen, the drawer should animate to its final state.
- The opening and closing behavior should feel close to the Pixel launcher interaction model.
- Overscroll or rebound behavior should apply only to the app list, not to the entire drawer container.
- While the drawer is opening or visible, the library grid must not respond to user scrolling.
- If the app list reaches its top during an ongoing downward list scroll, the drawer must stay fully open for that gesture.
- Closing the drawer from the app list should only start on a new downward gesture that begins while the list is already at the top.
- If the app list is already at the top and the user starts a new downward gesture, the drawer itself should follow the finger and enter the closing interaction.
- The app drawer should show installed apps that expose a direct launch entry point through `ACTION_MAIN + CATEGORY_LAUNCHER`.
- The app drawer should source apps from a simple launcher query based on `ACTION_MAIN + CATEGORY_LAUNCHER`.
- The app drawer should launch the resolved activity component directly on the current display.
- Long-pressing an app in the drawer should open app details, matching common launcher expectations.
- The drawer should handle temporary empty or loading states gracefully instead of rendering a blank list.

## Activity Coordination

- The app must provide a dedicated activity set for the two physical screens.
- The top-screen and bottom-screen activities must start together.
- The top-screen and bottom-screen activities must exit together.
- If the system returns only the top-screen task when Home is pressed, the launcher flow must actively restore the bottom-screen task instead of requiring a cold restart.
- The visible top-screen activity should be the app's `CATEGORY_HOME` surface on the primary display.
- The visible bottom-screen activity should be the app's `CATEGORY_SECONDARY_HOME` surface on the secondary display.
- A per-display safety-net task is allowed as a fallback so an emptied display stack can restore the dual-screen home state instead of leaving a blank or inconsistent screen.
- Jergal-owned tasks should be excluded from Recents so the launcher does not appear as a swipe-away recent app entry.
- Pressing Home while Jergal is already the visible dual-screen home should be idempotent and should not relaunch or reset the current launcher UI state.
- A lightweight launcher activity may be used to place both screen activities on the correct displays.
- Shared in-process state is acceptable for synchronizing the currently highlighted game.
- A process-local coordinator is acceptable for mirrored shutdown behavior.

## Current Implementation Direction

- A launcher helper starts both home activities and selects displays by display ID when Jergal is opened from the app icon or when one display needs to restore its companion display.
- The app-icon entry activity should behave as a non-visual trampoline rather than a visible destination.
- The app package should qualify as an Android Home app and may request `RoleManager.ROLE_HOME` when the role is available and not yet held.
- Generic explicit launch intents are acceptable for app drawer launches.
- The top-screen activity may need to reassert the bottom-screen activity on resume because some devices restore only the default-display task when returning Home.
- The bottom-screen activity may need to reassert the top-screen activity on resume because some devices or gestures can return only the secondary home surface first.
- Safety-net activities may be launched as hidden per-display fallback tasks, but they should only restore the launcher pair when they re-enter the foreground after having been backgrounded.
- A freshly created safety-net task must immediately settle behind the real launcher surfaces and must never remain as a transparent overlay above the top-screen home when returning Home from another app.
- The bottom screen owns game browsing and the app drawer gesture.
- The top screen reflects the current bottom-screen selection.
- Shared selection state is stored in-process.
- Dual-screen lifecycle shutdown is coordinated in-process.

## Code Conventions

- Do not hardcode UI or feature colors inside Kotlin UI or business logic files.
- All runtime UI colors must come from the app theme or theme color roles.
- Theme palette definitions may exist in dedicated theme files only.
- Launcher-visible app enumeration should prefer manifest `<queries>` plus `queryIntentActivities()` over broad package visibility permissions.
- Context files, code comments, and documentation comments must always be written in English.
- This rule applies regardless of which language a contributor uses in AI chat.
- Public classes must have Javadoc or KDoc.
- Public methods must have Javadoc or KDoc.
