# Plugin Manager - User Guide & Quick Start

**Date:** February 12, 2026  
**Status:** ✅ READY TO USE

---

## Quick Start (3 Steps)

### Step 1: Configure Repository
1. Open Plugin Manager block
2. Go to **Repositories** tab
3. Click **Add Repository**
4. Enter GitHub URL - **two options:**
   - **Organization URL** (fetch ALL repositories):
     ```
     https://github.com/chaosnoteapp
     ```
   - **Specific Repository URL**:
     ```
     https://github.com/chaosnoteapp/chaosnote-plugins
     ```
5. Set prefix to filter repositories:
   - `*` = fetch all repos from organization
   - `plugins-` = only fetch repos starting with "plugins-"
6. Click **Add**

### Step 2: Fetch Plugins
1. Click **Fetch Plugins from Repositories** button
2. Wait for plugins to load
3. Switch to **Plugins** tab

### Step 3: Install/Update/Delete
- **Install**: Click **Install** button on any plugin
- **Update**: Click **Update to vX.X.X** if newer version available
- **Delete**: Click red delete icon to remove installed plugin

---

## Features Explained

### 🔧 Repositories Tab

#### What it does
Configure where to fetch plugins from (GitHub repositories)

#### How to use

**Add Repository:**
```
URL: https://github.com/chaosnoteapp/chaosnote-plugins
Prefix: plugins-
```
- `plugins-` means: only fetch releases starting with "plugins-"
- `*` means: fetch all releases from this repo

**Edit Repository:**
- Click **Edit** to modify URL or prefix
- Click **Save** to confirm

**Delete Repository:**
- Click delete icon to remove

**Fetch Plugins:**
- Click **Fetch Plugins from Repositories** button
- This queries all configured repos and displays found plugins in Plugins tab

---

### 📦 Plugins Tab

#### What it does
Display available plugins and manage installations

#### Plugin Status

**Not Installed:**
```
[Install] button
```

**Installed:**
```
[Installed v1.0.0] [Delete]
```

**Update Available:**
```
[Update to v2.0.0] [Delete]
```

#### Actions

**Install Plugin:**
1. Click **Install** button
2. Plugin downloads from GitHub release
3. JAR saved to `%APPDATA%/Chaosnote/plugins/plugin-name-version.jar`
4. Status changes to "Installed"

**Update Plugin:**
1. If newer version available, **Update** button appears
2. Click to update to latest version
3. Old version automatically deleted
4. New version installed

**Delete Plugin:**
1. Click red delete icon
2. JAR file removed from plugins directory
3. Status changes back to "Install"

#### Progress Indicator
During download/install:
- Shows progress bar
- Displays current action
- Shows error message if something fails

---

## Prefix Filtering Examples

### Example 1: Match specific prefix
```
Repository: https://github.com/chaosnoteapp/chaosnote-plugins
Prefix: plugins-
Results: plugins-search, plugins-console, plugins-code-editor
Excludes: simple-text, plugin-marketplace
```

### Example 2: Get all releases
```
Repository: https://github.com/chaosnoteapp/chaosnote-plugins
Prefix: *
Results: All releases (plugins-search, simple-text, everything)
```

### Example 3: Custom prefix
```
Repository: https://github.com/myuser/my-plugins
Prefix: my-
Results: my-plugin-1, my-plugin-2, etc.
```

---

## File Management

### Plugin Directory
```
%APPDATA%/Chaosnote/plugins/
```

### File Naming
```
plugin-name-version.jar
Example: plugins-search-0.1.0.jar
```

### Auto-creation
Plugin directory is automatically created if missing

### Manual Management
You can manually add/remove JAR files from plugins directory
- ChaosNote will detect changes on next restart

---

## GitHub Repository Setup

### GitHub Repository Setup

#### Repository URL Format

Plugin Manager now supports **TWO ways** to add repositories:

**Option 1: Organization URL (Recommended)**
```
https://github.com/chaosnoteapp

This fetches ALL repositories from the organization.
Then use prefix filter to select which ones you want.
```

**Option 2: Specific Repository URL**
```
https://github.com/chaosnoteapp/chaosnote-plugins

This fetches releases from only this specific repository.
```

**Prefix Filtering Examples:**

With organization URL `https://github.com/chaosnoteapp`:
```
Prefix: *
Result: All repositories → plugins-search, plugins-console, simple-text, etc.

Prefix: plugins-
Result: Only repos starting with "plugins-" → plugins-search, plugins-console, plugins-code-editor

Prefix: simple-
Result: Only repos starting with "simple-" → simple-text
```

**Why Two Options?**

1. **Organization URL** - Great when you want to browse all available plugins and filter by prefix
2. **Specific Repository** - Great when you know exactly which repository you want

**Valid Format Examples:**
```
✅ https://github.com/chaosnoteapp                           (Organization - fetch all)
✅ https://github.com/chaosnoteapp/chaosnote-plugins         (Specific repository)
✅ https://github.com/john-doe/my-plugins                    (User account, specific repo)
✅ github.com/chaosnoteapp                                   (Without protocol)

❌ https://api.github.com/repos/user/repo                    (Don't use API URL directly)
```
```
Input:  https://github.com/chaosnoteapp/chaosnote-plugins
Output: https://api.github.com/repos/chaosnoteapp/chaosnote-plugins/releases
```

```
your-repo/
├── plugins-search/          (optional, just for organization)
├── plugins-console/
└── README.md

releases/
├── v0.1.0
│   └── plugins-search-0.1.0.jar
├── v1.0.0
│   └── plugins-console-1.0.0.jar
```

### Creating a Release
1. Go to GitHub repo → Releases
2. Click "Create a new release"
3. Tag: `v1.0.0` (or any version)
4. Upload JAR file as asset
5. Click "Publish release"

### JAR File Naming
Recommendation:
```
{project-name}-{version}.jar
Example: plugins-search-0.1.0.jar
```

---

## Troubleshooting

### Issue: "Error fetching plugins" or "HTTP 406"

**Possible Causes:**
1. **Invalid GitHub URL** - Missing repository name
2. GitHub API issue
3. No releases in repository
4. Repository is private (no auth)
5. GitHub API rate limit

**Solutions:**
- Verify GitHub URL has both username AND repository name
  - ❌ Wrong: `https://github.com/chaosnoteapp`
  - ✅ Correct: `https://github.com/chaosnoteapp/chaosnote-plugins`
- Ensure repository exists and is public
- Check that repository has at least one release with JAR asset
- Wait a few minutes if rate limit exceeded (60 requests/hour)
- Check internet connection

### Issue: "No JAR files found in releases"

**Solution:**
- Ensure release has a `.jar` file attached as an asset
- Check file naming and extension

### Issue: "Prefix not matching my releases"

**Solution:**
- Check release/file names match prefix
- Use `*` to match all releases
- Example: prefix `plugins-` won't match `simple-text`

### Issue: "Plugin directory not found"

**Solution:**
- Automatically created in `%APPDATA%/Chaosnote/plugins`
- If missing, restart ChaosNote
- Check Windows permission to write to AppData

---

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Switch to Repositories tab | Click tab |
| Switch to Plugins tab | Click tab |
| Edit repository | Click Edit |
| Delete repository | Click delete icon |
| Fetch plugins | Click button |
| Install plugin | Click Install |
| Update plugin | Click Update |
| Delete plugin | Click delete icon |

---

## Best Practices

### 1. Repository Setup
- Use descriptive repository names
- Include README with instructions
- Keep releases organized with semantic versioning

### 2. Naming Convention
```
{prefix}-{name}-{version}.jar
plugins-search-0.1.0.jar ✅
my-plugin-1.2.3.jar ✅
plugin.jar ❌ (no version)
```

### 3. Backup
- Before deleting, note installed version
- Keep backup JAR files locally
- Use GitHub releases as source of truth

### 4. Multiple Repositories
- Can add multiple repos
- Plugins shown in one combined list
- Useful for organizing different plugin sets

---

## Advanced

### Manual Plugin Installation
1. Download JAR file
2. Save to `%APPDATA%/Chaosnote/plugins/`
3. Name format: `name-version.jar`
4. Restart ChaosNote

### Remove Plugin Manually
1. Delete JAR from `%APPDATA%/Chaosnote/plugins/`
2. ChaosNote detects on next restart

### Environment Variable
```
APPDATA = C:\Users\YourUsername\AppData\Roaming
Full path: C:\Users\YourUsername\AppData\Roaming\Chaosnote\plugins
```

---

## FAQ

**Q: Can I use private repositories?**
A: Currently no auth support. Requires public repository.

**Q: Can I install multiple versions of same plugin?**
A: No, only one version per plugin. New install replaces old.

**Q: How often should I check for updates?**
A: Manual only - click Fetch Plugins to refresh list.

**Q: Can I rollback to previous version?**
A: No built-in rollback. Manual download required.

**Q: Do plugins update automatically?**
A: No, all updates are manual.

**Q: What if GitHub is down?**
A: Plugin Manager will show error. Try again later.

---

## Support

For issues or feature requests, contact the development team or create a GitHub issue.

---

**Plugin Manager v1.0** ✅ Ready to use!

