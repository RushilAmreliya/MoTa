# MoTA Scholarship App – UI Design Document

## 1. Design Principles
- Clean, practical and government-style
- Human-looking (not overly perfect or AI-generated)
- No neon colours
- Slightly dense layout (similar to DigiLocker / UMANG)
- Easy to understand for ST students
- Mobile-first

## 2. Color Palette

| Name              | Hex Code   | Usage                          |
|-------------------|------------|--------------------------------|
| Primary Green     | #1B5E20    | App bar, buttons, selected tab |
| Accent Orange     | #E65100    | Warnings, secondary actions    |
| Background        | #F7F7F7    | Screen background              |
| Card Background   | #FFFFFF    | Scheme & document cards        |
| Text Primary      | #212121    | Main headings & body text      |
| Text Secondary    | #616161    | Subtitles, timestamps          |
| Success           | #2E7D32    | Disbursed / Verified           |
| Warning           | #EF6C00    | Under Review / Pending         |
| Error             | #C62828    | Deficiency / Rejected          |
| Border / Divider  | #E0E0E0    | Card borders, dividers         |

## 3. Typography
- Use default Material 3 typography
- Headings: Medium / SemiBold
- Body: Regular
- Keep text size readable (minimum 14sp for body)

## 4. Main Screens

### 4.1 Dashboard (Home)
- Top App Bar: “MoTA Scholarships”
- Greeting: “Hello, [Student Name]” + small ST badge
- Section title: “My Applications”
- 5 Scheme Cards:
  - Pre-Matric
  - Post-Matric
  - Top Class
  - National Fellowship
  - National Overseas
- Each card contains:
  - Icon
  - Scheme name
  - Short subtitle (class/course + year)
  - Status chip
  - “Last updated X hours ago”
- Bottom Navigation
- Floating Action Button for JAGO chat

### 4.2 Document Wallet
- Title: “My Documents”
- Search bar
- List of document cards:
  - Document name
  - “DigiLocker” tag
  - Upload date
  - Status (Verified / Pending)
  - View button
- “Upload Document” button at the bottom

### 4.3 JAGO Chat
- Simple chat interface
- Bot messages on left (light green tint)
- User messages on right (light gray)
- Quick suggestion chips:
  - Am I eligible?
  - Application status
  - Missing documents
  - Payment status
- Text input field at bottom

### 4.4 Profile
- Student name & photo placeholder
- ST status
- Linked schemes summary
- Logout option

## 5. Components to Use (Material 3)

- `CenterAlignedTopAppBar` or normal TopAppBar
- `ElevatedCard` for scheme & document cards
- `AssistChip` / `FilterChip` for status
- `NavigationBar` for bottom navigation
- `FloatingActionButton` for JAGO
- `OutlinedTextField` for search & chat input
- `Button` / `FilledTonalButton`

## 6. Status Chips

| Status         | Background Color | Text Color |
|----------------|------------------|------------|
| Disbursed      | #E8F5E9          | #2E7D32    |
| Verified       | #E8F5E9          | #2E7D32    |
| Under Review   | #FFF3E0          | #EF6C00    |
| Pending        | #FFF3E0          | #EF6C00    |
| Deficiency     | #FFEBEE          | #C62828    |

## 7. Bottom Navigation Items
1. Home
2. Documents
3. Chat (JAGO)
4. Profile

## 8. Notes for Human Look
- Do not make all cards exactly the same height
- Show at least one card with “Deficiency” status
- Use realistic timestamps (“Last updated 3 hours ago”)
- Keep spacing natural (12–16 dp)
- Avoid heavy shadows and glowing effects
- Prefer simple icons over complex illustrations

## 9. Recommended Libraries
- Jetpack Compose + Material 3
- Coil (for images if needed)
- Navigation Compose

---

**End of Design Document**# MoTA Scholarship App – UI Design Document

## 1. Design Principles
- Clean, practical and government-style
- Human-looking (not overly perfect or AI-generated)
- No neon colours
- Slightly dense layout (similar to DigiLocker / UMANG)
- Easy to understand for ST students
- Mobile-first

## 2. Color Palette

| Name              | Hex Code   | Usage                          |
|-------------------|------------|--------------------------------|
| Primary Green     | #1B5E20    | App bar, buttons, selected tab |
| Accent Orange     | #E65100    | Warnings, secondary actions    |
| Background        | #F7F7F7    | Screen background              |
| Card Background   | #FFFFFF    | Scheme & document cards        |
| Text Primary      | #212121    | Main headings & body text      |
| Text Secondary    | #616161    | Subtitles, timestamps          |
| Success           | #2E7D32    | Disbursed / Verified           |
| Warning           | #EF6C00    | Under Review / Pending         |
| Error             | #C62828    | Deficiency / Rejected          |
| Border / Divider  | #E0E0E0    | Card borders, dividers         |

## 3. Typography
- Use default Material 3 typography
- Headings: Medium / SemiBold
- Body: Regular
- Keep text size readable (minimum 14sp for body)

## 4. Main Screens

### 4.1 Dashboard (Home)
- Top App Bar: “MoTA Scholarships”
- Greeting: “Hello, [Student Name]” + small ST badge
- Section title: “My Applications”
- 5 Scheme Cards:
  - Pre-Matric
  - Post-Matric
  - Top Class
  - National Fellowship
  - National Overseas
- Each card contains:
  - Icon
  - Scheme name
  - Short subtitle (class/course + year)
  - Status chip
  - “Last updated X hours ago”
- Bottom Navigation
- Floating Action Button for JAGO chat

### 4.2 Document Wallet
- Title: “My Documents”
- Search bar
- List of document cards:
  - Document name
  - “DigiLocker” tag
  - Upload date
  - Status (Verified / Pending)
  - View button
- “Upload Document” button at the bottom

### 4.3 JAGO Chat
- Simple chat interface
- Bot messages on left (light green tint)
- User messages on right (light gray)
- Quick suggestion chips:
  - Am I eligible?
  - Application status
  - Missing documents
  - Payment status
- Text input field at bottom

### 4.4 Profile
- Student name & photo placeholder
- ST status
- Linked schemes summary
- Logout option

## 5. Components to Use (Material 3)

- `CenterAlignedTopAppBar` or normal TopAppBar
- `ElevatedCard` for scheme & document cards
- `AssistChip` / `FilterChip` for status
- `NavigationBar` for bottom navigation
- `FloatingActionButton` for JAGO
- `OutlinedTextField` for search & chat input
- `Button` / `FilledTonalButton`

## 6. Status Chips

| Status         | Background Color | Text Color |
|----------------|------------------|------------|
| Disbursed      | #E8F5E9          | #2E7D32    |
| Verified       | #E8F5E9          | #2E7D32    |
| Under Review   | #FFF3E0          | #EF6C00    |
| Pending        | #FFF3E0          | #EF6C00    |
| Deficiency     | #FFEBEE          | #C62828    |

## 7. Bottom Navigation Items
1. Home
2. Documents
3. Chat (JAGO)
4. Profile

## 8. Notes for Human Look
- Do not make all cards exactly the same height
- Show at least one card with “Deficiency” status
- Use realistic timestamps (“Last updated 3 hours ago”)
- Keep spacing natural (12–16 dp)
- Avoid heavy shadows and glowing effects
- Prefer simple icons over complex illustrations

## 9. Recommended Libraries
- Jetpack Compose + Material 3
- Coil (for images if needed)
- Navigation Compose

---

**End of Design Document**