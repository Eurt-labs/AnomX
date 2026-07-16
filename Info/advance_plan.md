# 🎯 AnomX - Advanced Features Implementation Plan

This document serves as the master planning blueprint for adding advanced capabilities to the **AnomX** application. It identifies existing unused layout hooks, maps out proposed feature upgrades, details step-by-step technical implementation procedures, and maintains a track record of architectural decisions.

---

## 📋 Proposed Advanced Features

Based on the codebase analysis, here are the key features we can implement to elevate AnomX from a simple SOS tool to an advanced personal safety system:

### 1. Safety Countdown Timer (SOS Delay)
* **Current State:** A layout file `dialog_countdown.xml` exists in the codebase but is not linked to any logic.
* **Proposed Logic:** 
  * Add a "Timed SOS" option to the dashboard.
  * When clicked, display a countdown dialog (e.g., 10 seconds) utilizing the `dialog_countdown.xml` layout.
  * Play a warning tone or emit small vibration pulses.
  * If the user presses "Cancel", the SOS is aborted. If the timer hits zero, the SOS is automatically dispatched.
  * Useful for walking in high-risk zones where the user wants to prepare an alert but cancel it if they arrive safely.

### 2. Live Location Breadcrumbs (Continuous GPS Updates)
* **Current State:** Listed as "In Development" in the README, and the toggle `opt_breadcrumbs` is saved in `SharedPreferences` via `AdvancedDashboardActivity` but has no functional backend.
* **Proposed Logic:**
  * When an SOS alert is active, start a background location tracking loop in `ShakeService` (using Google Play Services `requestLocationUpdates` with a 3-5 minute interval).
  * Periodically send updated GPS coordinates to the registered emergency contacts.
  * Stop tracking when the service is stopped by the user or when the target stops moving.

### 3. Contact Picker Integration
* **Current State:** Users must manually type phone numbers in the `MainActivity` input field.
* **Proposed Logic:**
  * Add a "Choose from Contacts" button next to the input.
  * Request `READ_CONTACTS` permission.
  * Launch the system contact picker using `ActivityResultContracts.PickContact`.
  * Extract the phone number and automatically insert it into the list.

### 4. Jetpack Compose UI Migration
* **Current State:** UI is built using XML layouts (`activity_main.xml`, etc.). Build features for Compose are turned off (`compose = false` in `app/build.gradle.kts`).
* **Proposed Logic:**
  * Enable Compose in the Gradle configuration.
  * Re-architect pages using Compose elements to achieve a sleek glassmorphic style matching the modern aesthetic.

---

## 🛠️ Step-by-Step Implementation Approach

Here is how we will proceed once you confirm the feature set:

### Phase 1: Interactive Alignment
* Clarify requirements and choose which features to build.

### Phase 2: Safety Countdown Timer Integration
1. Update `MainActivity.kt` to bind the Timed SOS action.
2. Initialize a `CountDownTimer` instance in a custom `AlertDialog` utilizing `dialog_countdown.xml`.
3. Link the timer completion to `sendEmergencyAlert()`.

### Phase 3: Continuous Location tracking (Breadcrumbs)
1. Modify `ShakeService.kt` to request continuous updates if `opt_breadcrumbs` is enabled in settings.
2. Implement a `LocationCallback` that matches location movements and sends automated updates.

---

## 🚀 Planning & Decision Track Record

Developers must document planning updates, scope adjustments, and chosen routes in this table to maintain transparency.

| Date (UTC) | Proposed Changes | Decisions Made | Status | Reference Files |
| :--- | :--- | :--- | :--- | :--- |
| **2026-07-16** | Initial Plan Proposal | Created planning blueprint listing Countdown Timer, Breadcrumbs tracking, Contact Picker, and Compose Migration. | ⏳ Awaiting User Feedback | [advance_plan.md](file:///c:/Users/Dhruv%20Saraswat/Documents/Projects/AnomX/Info/advance_plan.md) |

---

## 📥 User Feedback & Selection

Please reply with:
1. Which of the features above you want to implement first (e.g., Safety Countdown, Breadcrumbs, or Contact Picker).
2. Any other custom safety ideas or logic changes you want to introduce to this application.
