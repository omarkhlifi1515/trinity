# Blue Trinity ↔ Green Trinity Local Sync

## Database Sync Configuration

Blue Trinity is configured to read from the **same database** that Green Trinity Local uses.

### How It Works

1. **Green Trinity Local** creates/uses: `green trinity local/local_data.db`
2. **Blue Trinity** reads from: `green trinity local/local_data.db` (same file!)

### Database Path

Blue Trinity automatically detects the database path:
- Default: `C:\Users\msi\Documents\GitHub\trinity\green trinity local\local_data.db`
- Can be overridden with env var: `BLUE_TRINITY_DRIVE_PATH`

### Important Notes

⚠️ **If Green Trinity Local uses Postgres:**
- Blue Trinity will **NOT** see the data (it reads SQLite only)
- **Solution**: Make sure Green Trinity Local uses SQLite when you want Blue Trinity to work
- To use SQLite: Don't set `POSTGRES_URL` / `POSTGRES_SYNC_URL` env vars

✅ **If Green Trinity Local uses SQLite:**
- Blue Trinity will read the same database file
- Both apps see the same data in real-time
- Changes in Green Trinity appear in Blue Trinity immediately

### Tables Read by Blue Trinity

1. **`employee` table** - HR status (Active/Absent)
2. **`presence` table** - Real-time presence tracking (who marked present today)

### Real-Time Sync

- Blue Trinity reads directly from the SQLite file
- No sync delay - reads current data on each scan
- Manager runs periodically (default: daily, configurable)

### Testing Sync

1. **Start Green Trinity Local** (using SQLite):
   ```powershell
   cd "green trinity local"
   # Make sure NO POSTGRES_URL env var is set
   python app.py
   ```

2. **Add/update employees** in Green Trinity web UI

3. **Start Blue Trinity**:
   ```powershell
   cd "blue trinity"
   python blue_trinity_agent.py manager --once
   ```

4. **Check output** - should show correct Active/Absent counts

### Troubleshooting

**Blue Trinity can't find database?**
- Check path: `blue trinity/blue_trinity_agent.py` line 46
- Verify file exists: `green trinity local/local_data.db`
- Check file permissions

**Blue Trinity shows wrong counts?**
- Make sure Green Trinity is using SQLite (not Postgres)
- Check database file is not locked (close Green Trinity app)
- Verify `employee` and `presence` tables exist

**Data not syncing?**
- Both apps must use the same database file
- Green Trinity must be using SQLite mode
- Blue Trinity reads on each manager run (not continuous)

