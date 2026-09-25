# Deploy backend to Red Hat OpenShift

## 0. Push to GitHub
```bash
cd backend
git init            # if not already a repo
git add pom.xml src Dockerfile openshift .gitignore .dockerignore .env.example
git status          # make sure .env and target/ are NOT listed
git commit -m "backend: openshift-ready"
git push
```

## 1. Login and create project
```bash
oc login <openshift-api-url>
oc new-project myproject   # or: oc project myproject
```

## 2. Create secret (never commit real values)
```bash
oc create secret generic backend-secret \
  --from-literal=SPRING_DATASOURCE_URL='jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/test?sslMode=REQUIRED&allowPublicKeyRetrieval=true&useSSL=true' \
  --from-literal=SPRING_DATASOURCE_USERNAME='2TtmVo549Nrc4s2.root' \
  --from-literal=SPRING_DATASOURCE_PASSWORD='xxx' \
  --from-literal=JWT_SECRET='at-least-32-chars-long-secret-here' \
  --from-literal=CORS_ALLOWED_ORIGINS='https://your-frontend-route'
```

## 3a. Option A - Dockerfile build (recommended)
```bash
# From repo root / backend dir:
oc new-build --name backend --binary --strategy=docker
oc start-build backend --from-dir=. --follow
oc apply -f openshift/deployment.yaml
oc get route backend
```

## 3b. Option B - S2I Java build (no Dockerfile needed)
```bash
oc new-app registry.access.redhat.com/ubi8/openjdk-21:latest~https://github.com/<you>/<repo>.git \
  --context-dir=backend --name=backend \
  -e PORT=8080
oc set env deployment/backend --from=secret/backend-secret
oc expose svc/backend
```

## 4. Verify
```bash
oc get pods,svc,route
curl https://<backend-route>/actuator/health
curl -X POST https://<backend-route>/api/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{"name":"Test","email":"t@t.com","password":"secret123"}'
```

## Notes
- App listens on `0.0.0.0:${PORT:8080}` (OpenShift-compatible).
- Probes use `/actuator/health/liveness` and `/readiness`.
- Image runs as UID 185 (root group) so OpenShift's arbitrary UID works.
- Update `CORS_ALLOWED_ORIGINS` secret to your frontend URL after deploying it.
