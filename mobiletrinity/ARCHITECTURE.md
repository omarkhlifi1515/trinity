# MobileTrinity Architecture

Modern Android application using Jetpack Compose, Retrofit, and Room database.

## Technology Stack

- **UI Framework**: Jetpack Compose (Material Design 3)
- **HTTP Client**: Retrofit 2.9.0 with OkHttp
- **Local Database**: Room 2.5.2
- **Async**: Kotlin Coroutines
- **Build System**: Gradle KTS

## Architecture Layers

### 1. **API Layer** (`api/ApiService.kt`)
- Retrofit service interface defining all REST endpoints
- Data classes for requests/responses
- Communicates with AgentTrinity backend
- Endpoints: `/api/tasks`, `/api/tasks/{id}`, `/api/notifications`

### 2. **Data Layer** (`data/`)
- **TaskDatabase.kt**: Room database definition
  - `TaskDao`: Database access object (CRUD operations)
  - Singleton pattern for database instance
  - Cached local storage for offline support

- **TaskRepository.kt**: Single source of truth for data
  - Abstracts API and local database
  - Sync remote → local
  - Result<T> pattern for error handling

### 3. **UI Layer** (`ui/`)

#### Screens (`ui/screens/TaskListScreen.kt`)
- Main task list with filtering (all/todo/in_progress/completed)
- Floating Action Button for creating tasks
- Loading states and empty states
- Create task dialog

#### Components (`ui/components/TaskCard.kt`)
- TaskCard: Displays individual task with actions
- PriorityBadge: Visual priority indicator (high/medium/low)
- StatusDropdown: Change task status inline
- Reusable, composable building blocks

### 4. **Main Activity** (`MainActivity.kt`)
- Initializes Retrofit with base URL from env
- Initializes Room database
- Creates repository singleton
- Manages task state with LaunchedEffect for data loading
- Integrates all components

## Key Features

✅ **Modern Compose UI**: Entirely declarative, reactive UI  
✅ **Offline Support**: Local Room cache syncs with server  
✅ **Error Handling**: Result<T> pattern for safe error propagation  
✅ **Coroutines**: Async operations without callback hell  
✅ **Trinity Aesthetic**: Dark mode (#0f172a), neon accents (cyan, purple)  
✅ **MVVM-Ready**: Repository pattern supports ViewModel integration  

## Data Flow

```
User Action (Create/Edit/Delete Task)
        ↓
TaskListScreen Event Handler
        ↓
Repository.createTask() / updateTaskStatus() / deleteTask()
        ↓
ApiService (Retrofit) → HTTP Request to AgentTrinity
        ↓
TaskDao.insertTask() / updateTask() / deleteTask()
        ↓
Room Database (Local Cache)
        ↓
TaskListScreen recomposes with updated state
```

## Environment Setup

```bash
export AGENT_SERVER_URL="https://agent-trinity.onrender.com/"
```

The app reads `AGENT_SERVER_URL` from environment/BuildConfig:
- Production: Render-hosted agent server
- Development: `http://localhost:5000/`

## Database Schema

### Task Entity
```kotlin
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val priority: String,    // "high", "medium", "low"
    val status: String,       // "todo", "in_progress", "completed"
    val dueDate: String,
    val createdAt: String
)
```

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/tasks` | Fetch all tasks |
| POST | `/api/tasks` | Create new task |
| GET | `/api/tasks/{id}` | Get single task |
| PUT | `/api/tasks/{id}` | Update task details |
| DELETE | `/api/tasks/{id}` | Delete task |
| POST | `/api/tasks/{id}/status` | Update task status |

## Testing

Run unit tests:
```bash
./gradlew test
```

Run instrumented tests:
```bash
./gradlew connectedAndroidTest
```

## Future Enhancements

- [ ] ViewModel/LiveData integration for better state management
- [ ] Biometric authentication
- [ ] Voice input for creating tasks
- [ ] Push notifications via Firebase Cloud Messaging
- [ ] Offline queue for actions (sync when online)
- [ ] Dark/light theme toggle
- [ ] Task search and advanced filtering
