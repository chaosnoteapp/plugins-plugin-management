# Plugin Manager - Quick Reference

## File Locations

```
📁 plugins-plugin-managment/
├── 📄 PluginManagementPlugin.kt          Main plugin class
├── 📁 model/
│   └── PluginModels.kt                   Data classes
├── 📁 service/
│   ├── GitHubPluginService.kt            GitHub API integration
│   └── PluginFileManager.kt              File operations
├── 📁 viewmodel/
│   └── PluginManagementViewModel.kt      State management
├── 📁 ui/
│   ├── RepositoriesTab.kt                Repository config UI
│   └── PluginsListTab.kt                 Plugin list UI
├── 📄 PLUGIN_MANAGER_DOCS.md             Technical docs
└── 📄 USER_GUIDE.md                      User guide
```

## Data Flow

```
PluginManagementPlugin (Main UI)
    ↓
[Repositories Tab] ←→ PluginManagementViewModel ←→ [Plugins Tab]
                          ↓
                    PluginRepository List
                          ↓
                   GitHubPluginService (API)
                   PluginFileManager (Files)
```

## State Management

```kotlin
PluginManagementState
├── repositories: List<PluginRepository>
├── plugins: List<PluginInfo>
├── installProgress: PluginInstallProgress
├── isLoading: Boolean
├── error: String?
└── selectedTabIndex: Int
```

## User Actions Flow

```
Add Repository
├─ URL + Prefix
├─ Stored in ViewModel
└─ Ready for fetching

Fetch Plugins
├─ Query all repositories
├─ Parse GitHub API
├─ Compare with installed
└─ Display in list

Install Plugin
├─ Download JAR
├─ Save to AppData/Chaosnote/plugins
├─ Update status
└─ Show success

Update Plugin
├─ Delete old version
├─ Install new version
└─ Update status

Delete Plugin
├─ Remove JAR file
├─ Update status
└─ Show success
```

## Key Classes

### PluginManagementViewModel
```kotlin
fun addRepository(url: String, prefix: String)
fun removeRepository(repoId: String)
fun updateRepository(repoId: String, url: String, prefix: String)
fun fetchPlugins()
fun installPlugin(plugin: PluginInfo)
fun updatePlugin(plugin: PluginInfo)
fun deletePlugin(plugin: PluginInfo)
fun switchToPluginsTab()
fun switchToRepositoriesTab()
```

### GitHubPluginService
```kotlin
suspend fun fetchPlugins(repositories: List<PluginRepository>): List<PluginInfo>
private suspend fun fetchReleases(repoUrl: String): List<PluginRelease>
private fun parseReleases(jsonString: String): List<PluginRelease>
private fun fetchWithUserAgent(urlString: String): String
private fun String.toGitHubApiUrl(): String
```

### PluginFileManager
```kotlin
fun getInstalledPlugins(): Map<String, String>
suspend fun installPlugin(plugin: PluginInfo): Result<String>
suspend fun updatePlugin(plugin: PluginInfo): Result<String>
fun deletePlugin(plugin: PluginInfo): Result<String>
private suspend fun downloadJar(downloadUrl: String): File
```

## GitHub URL Examples

```
Valid URLs (Users):
- https://github.com/john-doe/my-plugins
- https://github.com/alice/plugin-collection
- http://github.com/bob/plugins

Valid URLs (Organizations):
- https://github.com/chaosnoteapp/chaosnote-plugins
- https://github.com/my-org/plugins-repo
- github.com/acme-corp/tools

Auto-converts to:
User:   https://api.github.com/repos/john-doe/my-plugins/releases
Org:    https://api.github.com/repos/chaosnoteapp/chaosnote-plugins/releases
```

## Prefix Filter Examples

```
Prefix: *
Result: All releases

Prefix: plugins-
Result: plugins-search, plugins-console, plugins-code-editor

Prefix: my-plugin
Result: my-plugin-1, my-plugin-special, my-plugin-v2
```

## Plugin Directory

```
%APPDATA%/Chaosnote/plugins/
  = C:\Users\YourName\AppData\Roaming\Chaosnote\plugins

File format:
  {plugin-name}-{version}.jar
  Example: plugins-search-0.1.0.jar
```

## Environment Variables

```kotlin
System.getenv("APPDATA")
  = C:\Users\YourName\AppData\Roaming
```

## Error Messages

| Error | Cause | Fix |
|-------|-------|-----|
| "Received HTML instead of JSON" | Invalid URL | Check GitHub URL format |
| "Error fetching releases" | Network issue | Check internet connection |
| "No JAR files found" | No JAR in release | Add JAR asset to release |
| "Failed to download" | Download link broken | Verify release still exists |
| "Failed to install" | File permission | Check AppData folder access |

## Status Indicators

```
Not Installed:
  [Install] button

Installed:
  [Installed v1.0.0] [Delete]

Update Available:
  [Update to v2.0.0] [Delete]

Installing/Updating:
  Progress bar + "Downloading..."

Error:
  Red error message + [Retry]

Success:
  Green confirmation message
```

## UI Components

### RepositoriesTab
- List of configured repositories
- Add/Edit/Delete buttons
- Fetch button
- URL and prefix configuration

### PluginsListTab
- Plugin cards with:
  - Name & description
  - Version badge
  - Status indicator
  - Action buttons
  - Progress tracking
  - Error messages

## Testing Checklist

```
[ ] Add repository
[ ] Edit repository
[ ] Delete repository
[ ] Fetch plugins
[ ] Install plugin
[ ] Check JAR created in AppData
[ ] Update plugin
[ ] Delete plugin
[ ] Check JAR removed
[ ] Test invalid URL error
[ ] Test network error handling
[ ] Test progress indicators
```

## Performance Notes

- **API calls**: Non-blocking (suspend functions)
- **File downloads**: Sequential (one at a time)
- **UI updates**: Reactive via StateFlow
- **Error recovery**: Graceful with user feedback

## Security Considerations

- GitHub URLs must be HTTPS
- API uses User-Agent header
- JAR files downloaded to sandboxed AppData
- No plugin execution (ChaosNote handles that)

## Future TODO

```kotlin
// TODO: Save repositories to preferences
// TODO: Implement plugin search
// TODO: Add dependency management
// TODO: Implement rollback feature
// TODO: Add automatic update checks
// TODO: Cache API responses
// TODO: Support parallel downloads
// TODO: Add plugin ratings
```

---

**Quick Start:**
1. Open Plugin Manager block
2. Go to Repositories tab
3. Click Add Repository
4. Enter GitHub URL + prefix
5. Click Fetch Plugins
6. Go to Plugins tab
7. Install/update/delete as needed

✅ **Ready to use!**

