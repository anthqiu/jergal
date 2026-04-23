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

## Activity Coordination

- The app must provide a dedicated activity set for the two physical screens.
- The top-screen and bottom-screen activities must start together.
- The top-screen and bottom-screen activities must exit together.
- A lightweight launcher activity may be used to place both screen activities on the correct displays.
- Shared in-process state is acceptable for synchronizing the currently highlighted game.
- A process-local coordinator is acceptable for mirrored shutdown behavior.

## Current Implementation Direction

- A launcher activity starts both screen activities and selects displays by display ID.
- The bottom screen owns game browsing and the app drawer gesture.
- The top screen reflects the current bottom-screen selection.
- Shared selection state is stored in-process.
- Dual-screen lifecycle shutdown is coordinated in-process.

## Code Conventions

- Do not hardcode UI or feature colors inside Kotlin UI or business logic files.
- All runtime UI colors must come from the app theme or theme color roles.
- Theme palette definitions may exist in dedicated theme files only.
- Context files, code comments, and documentation comments must always be written in English.
- This rule applies regardless of which language a contributor uses in AI chat.
- Public classes must have Javadoc or KDoc.
- Public methods must have Javadoc or KDoc.
