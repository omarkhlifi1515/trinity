# Trinity Deployment Checklist

## Pre-Deployment Verification

### Local Testing

- [ ] **WebTrinity**
  ```bash
  cd webtrinity
  pip install -r requirements.txt
  python run.py
  # Visit http://localhost:5000
  # Test: Register → Login → Dashboard
  ```

- [ ] **AgentTrinity**
  ```bash
  cd agenttrinity
  pip install -r requirements.txt
  # Test AI brain
  python agent.py ask "Create task for the team"
  # Start server (in another terminal)
  python agent_server.py
  # Test API: curl -H "X-API-Key: $AGENT_API_KEY" http://localhost:5000/create_task
  ```

- [ ] **MobileTrinity**
  ```bash
  cd mobiletrinity
  ./gradlew assembleDebug
  # Deploy to emulator/device and verify task list loads
  ```

### Environment Variables Setup

Create `.env` files in webtrinity/ and agenttrinity/:

**webtrinity/.env**
```env
DATABASE_URL=postgresql://user:password@localhost/trinity_db
SECRET_KEY=your-super-secret-key-min-32-chars
FLASK_ENV=production
```

**agenttrinity/.env**
```env
DATABASE_URL=postgresql://user:password@localhost/trinity_db
AGENT_API_KEY=your-agent-api-key-min-32-chars
OPENAI_API_KEY=sk-...your-actual-openai-key
AGENT_SERVER_URL=http://localhost:5000
```

---

## Render Deployment Steps

### 1. Connect Repository
- [ ] Go to [render.com](https://render.com)
- [ ] Connect GitHub account
- [ ] Select `trinity` repository

### 2. Create WebTrinity Service
- [ ] Create new **Web Service**
- [ ] Repository: `trinity`
- [ ] Root Directory: `webtrinity`
- [ ] Runtime: Python 3.11
- [ ] Build Command: `pip install -r requirements.txt`
- [ ] Start Command: `gunicorn wsgi:app`

**Environment Variables** (Add in Render dashboard):
```
DATABASE_URL=postgresql://[user]:[password]@[host]:[port]/trinity
SECRET_KEY=[generate-random-32-char-string]
FLASK_ENV=production
```

### 3. Create PostgreSQL Database
- [ ] Create **PostgreSQL** instance
- [ ] Save connection URL as `DATABASE_URL`
- [ ] Share same DATABASE_URL with both services

### 4. Create AgentTrinity Service
- [ ] Create new **Web Service**
- [ ] Repository: `trinity`
- [ ] Root Directory: `agenttrinity`
- [ ] Runtime: Python 3.11
- [ ] Build Command: `pip install -r requirements.txt`
- [ ] Start Command: `gunicorn agent_server:app`

**Environment Variables** (Add in Render dashboard):
```
DATABASE_URL=postgresql://[same-as-webtrinity]
AGENT_API_KEY=[generate-random-32-char-string]
OPENAI_API_KEY=sk-[your-actual-openai-api-key]
AGENT_SERVER_URL=https://[agent-trinity-service-name].onrender.com
```

### 5. Update WebTrinity Environment
- [ ] Add `AGENT_SERVER_URL` to webtrinity environment:
```
AGENT_SERVER_URL=https://[agent-trinity-service-name].onrender.com
```

---

## Post-Deployment Testing

### WebTrinity Smoke Tests
- [ ] Visit `https://[webtrinity-service].onrender.com`
- [ ] Register new user
- [ ] Login with credentials
- [ ] Create task on dashboard
- [ ] Verify task appears in list
- [ ] Logout successfully

### AgentTrinity API Tests
```bash
# Test with real API key
API_KEY="[your-agent-api-key]"
SERVER="https://[agent-service].onrender.com"

# Create task
curl -X POST "$SERVER/create_task" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","description":"Testing deployment"}'

# Send notification
curl -X POST "$SERVER/send_notification" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"user_id":1,"message":"Test notification"}'

# Update status
curl -X POST "$SERVER/update_status" \
  -H "X-API-Key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"task_id":1,"status":"completed"}'
```

### MobileTrinity Configuration
- [ ] Update `AGENT_SERVER_URL` in app build config or environment
- [ ] Build release APK: `./gradlew bundleRelease`
- [ ] Test on physical device with production API server

---

## Security Verification

- [ ] All DATABASE_URL values are real PostgreSQL endpoints (not localhost)
- [ ] SECRET_KEY is cryptographically random (min 32 chars)
- [ ] AGENT_API_KEY is cryptographically random (min 32 chars)
- [ ] OPENAI_API_KEY is valid and not expired
- [ ] No `.env` files committed to Git (check `.gitignore`)
- [ ] No hardcoded credentials in source code
- [ ] HTTPS enabled on all Render URLs (automatic)
- [ ] CORS configured if cross-origin calls needed

---

## Monitoring & Maintenance

### Render Dashboard
- [ ] Enable auto-deploys on Git push
- [ ] Set up email alerts for failures
- [ ] Monitor resource usage (memory, CPU)
- [ ] Review logs daily first week

### Database Health
- [ ] Test connection: `psql $DATABASE_URL -c "SELECT 1;"`
- [ ] Check table sizes: `SELECT * FROM pg_tables;`
- [ ] Verify backups are enabled

### API Monitoring
- [ ] Check OpenAI usage at openai.com/account/usage
- [ ] Monitor Render logs for errors
- [ ] Set up error tracking (e.g., Sentry)

---

## Troubleshooting Quick Reference

| Issue | Solution |
|-------|----------|
| **500 errors on webtrinity** | Check `DATABASE_URL` and `SECRET_KEY` in Render env |
| **Agent API returns 401** | Verify `X-API-Key` header matches `AGENT_API_KEY` |
| **OpenAI rate limits** | Implement request throttling or upgrade API plan |
| **Mobile app can't connect** | Verify `AGENT_SERVER_URL` is https and accessible |
| **Database connection timeout** | Check Render PostgreSQL IP whitelist settings |
| **Procfile command fails** | Ensure root directory matches service config |

---

## Rollback Plan

If deployment has critical issues:

1. **Immediate**: Update Render environment variables
   - Disable new features temporarily
   - Revert to previous working commit

2. **Short-term**: 
   ```bash
   git revert [problematic-commit-hash]
   git push  # Render auto-redeploys
   ```

3. **Investigation**:
   - Review Render logs
   - Check database consistency
   - Verify API responses

---

## Success Criteria

✅ **Trinity is production-ready when:**

- [ ] WebTrinity loads with authentication working
- [ ] Dashboard displays with zero errors
- [ ] Tasks can be created and viewed
- [ ] AgentTrinity API responds to all endpoints
- [ ] Natural language commands are processed by AI
- [ ] MobileTrinity connects and syncs tasks
- [ ] No sensitive data in logs
- [ ] Performance: < 200ms response time
- [ ] Database queries < 50ms
- [ ] All three services pass security audit

---

**Deployment Date**: ___________  
**Deployed By**: ___________  
**Notes**: ___________

