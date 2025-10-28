# Erinium Faction GUI Components Library

This directory contains all SVG texture components extracted from the FactionMenuScreen UI that can be used to replace `g.fill()` rendering with texture-based rendering.

## Directory Structure

```
components/
├── common/          # Common UI elements used across multiple pages
├── members/         # Members page specific components
├── quests/          # Quests page specific components
├── territory/       # Territory page specific components
├── alliances/       # Alliances page specific components
├── permissions/     # Permissions page specific components
├── settings/        # Settings page specific components
├── shop/            # Shop page specific components
├── level/           # Level page specific components
└── chest/           # Chest page specific components
```

---

## Color Palette Reference

### Primary Colors
- **Cyan/Bright**: `#00d2ff` - Highlights, borders, primary text
- **Dark Background**: `#16161f`, `#1e1e2e`, `#1a1a2e` - Container backgrounds
- **Purple Accent**: `#a855f7` - Progress fills, important elements
- **Blue Primary**: `#667eea` - Buttons, selections
- **Blue Hover**: `#7a8eee` - Button hover state

### Role/Status Colors
- **Leader**: `#ec4899` (pink)
- **Officer**: `#a855f7` (purple)
- **Member**: `#3b82f6` (blue)
- **Recruit**: `#6a6a7e` (gray)
- **Online**: `#10b981` (green)
- **Offline**: `#6a6a7e` (gray)

### Information Colors
- **Primary Text**: `#ffffff` (white)
- **Secondary Text**: `#a0a0c0` (light gray)
- **Tertiary Text**: `#9a9aae` (medium gray)
- **Error/Danger**: `#ef4444`, `#ff4444` (red)
- **Gold/Resources**: `#fbbf24` (yellow)
- **Success**: `#10b981` (green)

### UI State Colors
- **Hover Background**: `#667eea` at 25% opacity
- **Hover Bright**: `#3a3a4e`
- **Disabled**: `#6a6a7e`
- **Border/Glow**: `#667eea` at 30-50% opacity

---

## Components Catalog

## 1. Common Components (`common/`)

### Buttons

#### Primary Buttons
| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-primary-normal.svg` | 88x17 | Primary button normal state | Action buttons like "Invite", "Save" |
| `button-primary-hover.svg` | 88x17 | Primary button hover state | Hover effect for primary actions |

**Colors**: Background `#667eea` (normal) / `#7a8eee` (hover), Top border `#00d2ff`

#### Secondary Buttons
| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-secondary-normal.svg` | 88x17 | Secondary button normal state | Less prominent actions like "Manage", "Reset" |
| `button-secondary-hover.svg` | 88x17 | Secondary button hover state | Hover effect for secondary actions |

**Colors**: Background `#2a2a3e` (normal) / `#3a3a4e` (hover), Border `#667eea`

#### Danger Buttons
| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-danger-normal.svg` | 88x17 | Danger button normal state | Destructive actions like "Leave", "Disband" |
| `button-danger-hover.svg` | 88x17 | Danger button hover state | Hover effect for danger actions |

**Colors**: Border `#ef4444` (normal) / `#ff4444` (hover)

### Navigation Buttons

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `nav-button-normal.svg` | 68x17 | Navigation button normal state | Sidebar navigation items |
| `nav-button-hover.svg` | 68x17 | Navigation button hover state | Hover effect on nav items |
| `nav-button-selected.svg` | 68x17 | Navigation button selected state | Currently active page indicator |

**Colors**: Normal `#2a2a3e`, Selected `#667eea` with cyan top bar, Hover adds subtle blue overlay

### Close Button

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `close-button-normal.svg` | 11x11 | Close button normal state | Window close button |
| `close-button-hover.svg` | 11x11 | Close button hover state | Hover effect on close |

**Colors**: `#ef4444` (normal) / `#ff4444` (hover)

### Progress Bars

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `progressbar-empty.svg` | 60x4 | Empty progress bar | Base for all progress bars |
| `progressbar-filled-70.svg` | 60x4 | Progress bar at 70% (example) | Power bars, XP bars |

**Colors**: Empty `#2a2a3e`, Fill `#a855f7` (purple - adjust color based on context)

**Note**: Filled percentage can be dynamically rendered by adjusting the width of the fill rectangle.

---

## 2. Members Page (`members/`)

### Member Cards

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `member-card-normal.svg` | 275x30 | Member list item normal state | Display member in list |
| `member-card-hover.svg` | 275x30 | Member list item hover state | Hover effect on member entry |

**Colors**: Background `#1e1e2e`, Hover overlay `#667eea` at 25%

### Action Buttons

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-promote-normal.svg` | 16x16 | Promote member button | Promote to higher rank |
| `button-promote-hover.svg` | 16x16 | Promote button hover | Hover state |
| `button-demote-normal.svg` | 16x16 | Demote member button | Demote to lower rank |
| `button-demote-hover.svg` | 16x16 | Demote button hover | Hover state |
| `button-kick-normal.svg` | 16x16 | Kick member button | Remove member from faction |
| `button-kick-hover.svg` | 16x16 | Kick button hover | Hover state |

**Colors**:
- Promote: `#10b981` (green)
- Demote: `#fbbf24` (yellow)
- Kick: `#ef4444` (red)

**Icons**:
- Promote: Up arrow
- Demote: Down arrow
- Kick: X symbol

---

## 3. Quests Page (`quests/`)

### Quest Cards

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `quest-card-daily-normal.svg` | 275x60 | Daily quest card normal | Display daily quest |
| `quest-card-daily-hover.svg` | 275x60 | Daily quest card hover | Hover effect |
| `quest-card-daily-completed.svg` | 275x60 | Daily quest completed | Quest finished state |
| `quest-card-weekly-normal.svg` | 275x60 | Weekly quest card normal | Display weekly quest |

**Colors**:
- Daily: Top border `#00d2ff` (cyan)
- Weekly: Top border `#fbbf24` (yellow)
- Completed: Border `#10b981` (green)

### Quest Progress Bars

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `questbar-blue-empty.svg` | 250x8 | Empty quest progress bar | Base for daily quests |
| `questbar-blue-filled-64.svg` | 250x8 | Quest progress at 64% (example) | Daily quest progress |
| `questbar-green-filled-100.svg` | 250x8 | Quest completed (100%) | Completed quest indicator |

**Colors**:
- Empty: `#2a2a3e`
- Daily Fill: `#00d2ff` (cyan)
- Weekly Fill: `#fbbf24` (yellow)
- Completed: `#10b981` (green)

### Claim Button

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-claim-normal.svg` | 60x17 | Claim reward button | Claim quest rewards |
| `button-claim-hover.svg` | 60x17 | Claim button hover | Hover state |

**Colors**: `#10b981` (green)

---

## 4. Territory Page (`territory/`)

### Claim Cards

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `claim-card-normal.svg` | 275x26 | Territory claim list item | Display claimed chunk |
| `claim-card-hover.svg` | 275x26 | Claim card hover | Hover effect |

**Colors**: Background `#1e1e2e`, Border `#667eea`

### Stat Cards

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `stat-card-normal.svg` | 92x32 | Territory stat card | Display chunks/max/power stats |

**Colors**: Generic border `#667eea` - customize top border color based on stat:
- Chunks: `#a855f7` (purple)
- Max: `#00d2ff` (cyan)
- Power: `#10b981` (green)

---

## 5. Alliances Page (`alliances/`)

### Alliance Cards

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `alliance-card-normal.svg` | 275x37 | Alliance list item | Display allied faction |
| `alliance-card-hover.svg` | 275x37 | Alliance card hover | Hover effect |

**Colors**: Background `#1e1e2e`, Top border `#00d2ff`

### Alliance Buttons

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-add-alliance-normal.svg` | 88x17 | Add alliance button | Create new alliance |
| `button-add-alliance-hover.svg` | 88x17 | Add alliance hover | Hover state |
| `button-remove-normal.svg` | 16x16 | Remove alliance button | Break alliance |
| `button-remove-hover.svg` | 16x16 | Remove alliance hover | Hover state |

**Colors**:
- Add: `#10b981` (green) with plus icon
- Remove: `#ef4444` (red) with minus icon

---

## 6. Permissions Page (`permissions/`)

### Checkboxes

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `checkbox-unchecked.svg` | 10x10 | Checkbox unchecked state | Permission disabled |
| `checkbox-checked.svg` | 10x10 | Checkbox checked state | Permission enabled |
| `checkbox-hover.svg` | 10x10 | Checkbox hover state | Hover effect |

**Colors**:
- Unchecked: `#2a2a3e`
- Checked: `#10b981` (green) with white checkmark
- Hover: Enhanced border `#667eea`

---

## 7. Settings Page (`settings/`)

### Input Fields

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `input-field-normal.svg` | 200x15 | Text input normal state | Faction name, description fields |
| `input-field-focus.svg` | 200x15 | Text input focus state | Active input field |

**Colors**:
- Normal: Background `#2a2a3e`, Border `#667eea` at 30%
- Focus: Border `#00d2ff` at 80%

### Toggle Switches

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `toggle-off.svg` | 30x14 | Toggle switch OFF state | "Open to Join" disabled |
| `toggle-on.svg` | 30x14 | Toggle switch ON state | "Open to Join" enabled |

**Colors**:
- OFF: Background `#2a2a3e`, Circle `#6a6a7e` (left position)
- ON: Background `#10b981`, Circle `#ffffff` (right position)

---

## 8. Shop Page (`shop/`)

### Shop Items

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `shop-item-normal.svg` | 275x52 | Shop item card | Display purchasable item |
| `shop-item-hover.svg` | 275x52 | Shop item hover | Hover effect |

**Colors**: Background `#1e1e2e`, Icon placeholder `#a855f7` at 40-60%

### Purchase Button

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `button-purchase-normal.svg` | 60x17 | Purchase button | Buy item from shop |
| `button-purchase-hover.svg` | 60x17 | Purchase hover | Hover state |

**Colors**: `#fbbf24` (yellow/gold)

---

## 9. Level Page (`level/`)

### Level Badge

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `level-badge.svg` | 50x48 | Hexagonal level badge | Display faction level prominently |

**Colors**: Gradient from `#fbbf24` to `#f59e0b` (yellow/gold)

**Shape**: Hexagon with inner shadow for depth effect

---

## 10. Chest Page (`chest/`)

### Inventory Slots

| File | Dimensions | Description | Usage |
|------|------------|-------------|-------|
| `inventory-slot-empty.svg` | 16x16 | Empty inventory slot | Faction chest slot background |
| `inventory-slot-hover.svg` | 16x16 | Inventory slot hover | Hover effect on slot |

**Colors**:
- Empty: Background `#2a2a3e`, Border `#667eea` at 30%
- Hover: Overlay `#667eea` at 40%, Border `#00d2ff`

---

## Implementation Guidelines

### 1. How to Use These Components

#### Basic Rendering
Replace `g.fill()` calls with texture rendering:

```java
// OLD CODE (g.fill)
g.fill(0xFF667eea);
g.fillRect(x, y, width, height);

// NEW CODE (texture-based)
RenderSystem.setShaderTexture(0, BUTTON_PRIMARY_NORMAL);
GuiComponent.blit(poseStack, x, y, 0, 0, width, height, width, height);
```

#### State-Based Rendering
Switch textures based on interaction state:

```java
ResourceLocation texture = isHovered ? BUTTON_PRIMARY_HOVER : BUTTON_PRIMARY_NORMAL;
RenderSystem.setShaderTexture(0, texture);
GuiComponent.blit(poseStack, x, y, 0, 0, width, height, width, height);
```

### 2. Dynamic Elements

#### Progress Bars
For progress bars, use the empty base and render a filled overlay:

```java
// Render empty bar
RenderSystem.setShaderTexture(0, PROGRESSBAR_EMPTY);
GuiComponent.blit(poseStack, x, y, 0, 0, totalWidth, height, totalWidth, height);

// Render filled portion
int filledWidth = (int)(totalWidth * percentage);
RenderSystem.setShaderTexture(0, PROGRESSBAR_FILLED);
GuiComponent.blit(poseStack, x, y, 0, 0, filledWidth, height, totalWidth, height);
```

#### Color Variants
Some components come in generic form. To use different colors:
- Modify the SVG fill colors before loading
- Create color variants (e.g., `stat-card-purple.svg`, `stat-card-cyan.svg`)
- Use color overlays in rendering code

### 3. Scaling Considerations

All dimensions are based on the scaled coordinate system:
- Base GUI size: 400x270
- Use `sw()` and `sh()` scaling functions from code
- SVG files are vector-based and scale cleanly
- Maintain aspect ratios when scaling

### 4. Text Rendering

These texture components do NOT include text - text is still rendered separately:
```java
// Render button texture
RenderSystem.setShaderTexture(0, BUTTON_PRIMARY_NORMAL);
GuiComponent.blit(...);

// Render button text on top
font.draw(poseStack, "Button Text", x + centerOffsetX, y + centerOffsetY, 0xFFFFFFFF);
```

### 5. Custom Variations

Players can customize the UI by:
1. Creating resource pack with modified SVGs
2. Replacing textures in `assets/erinium_faction/textures/gui/components/`
3. Maintaining same dimensions and file names
4. Using custom colors, gradients, shapes while preserving layout

---

## Component Priority for Implementation

### High Priority (Most Used)
1. **Common buttons** - Used everywhere
2. **Progress bars** - Power, XP, quests
3. **Navigation buttons** - Sidebar
4. **Card backgrounds** - All list items

### Medium Priority
5. **Member/Quest/Alliance cards** - Page-specific lists
6. **Stat cards** - Overview and Territory
7. **Input fields and toggles** - Settings

### Lower Priority
8. **Action buttons** (promote/demote/kick) - Small but impactful
9. **Checkboxes** - Permissions only
10. **Level badge** - One location
11. **Inventory slots** - Chest page only

---

## File Naming Convention

All component files follow this pattern:
```
{component-type}-{variant}-{state}.svg
```

Examples:
- `button-primary-hover.svg` - Primary button in hover state
- `quest-card-daily-normal.svg` - Daily quest card normal state
- `checkbox-checked.svg` - Checkbox in checked state

---

## Total Component Count

- **Common**: 13 files
- **Members**: 8 files
- **Quests**: 9 files
- **Territory**: 3 files
- **Alliances**: 6 files
- **Permissions**: 3 files
- **Settings**: 4 files
- **Shop**: 4 files
- **Level**: 1 file
- **Chest**: 2 files

**Total: 53 SVG components**

---

## Future Expansion

Additional components that could be added:
- Scrollbar components (track, thumb in different states)
- Header/title backgrounds
- Container/panel backgrounds
- Modal overlay backgrounds
- Tooltip backgrounds
- Notification badges
- Loading spinners
- Rank indicators (Officer/Member/Recruit badges)

---

## Support & Customization

For players creating texture packs:
1. All SVG files can be opened in any vector editor (Inkscape, Illustrator, etc.)
2. Maintain original dimensions for proper alignment
3. Test in-game to ensure colors match your theme
4. Share your texture packs with the community!

For developers:
- All components extracted from analysis of `FactionMenuScreen.java` and page classes
- Colors and dimensions match the original `g.fill()` rendering
- Components are designed to be drop-in replacements
- See Java code comments for specific usage locations

---

*Generated by Claude Code based on comprehensive analysis of the Erinium Faction GUI codebase*
