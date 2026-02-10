# Deployment Guide

This guide covers various deployment strategies for the RedDevil Analytics Backend application.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Deployment Options](#deployment-options)
  - [Docker Deployment](#docker-deployment)
  - [Kubernetes Deployment](#kubernetes-deployment)
  - [Cloud Platforms](#cloud-platforms)
- [Production Checklist](#production-checklist)
- [Monitoring and Logging](#monitoring-and-logging)
- [Backup and Recovery](#backup-and-recovery)

## Prerequisites

Before deploying to production, ensure you have:

- [ ] Domain name and SSL certificates
- [ ] Database instance (PostgreSQL 14+)
- [ ] Redis instance for caching
- [ ] API keys for all external providers
- [ ] Monitoring and alerting setup
- [ ] Backup strategy in place
- [ ] CI/CD pipeline configured

## Environment Configuration

### Production Environment Variables

Create a secure `.env` file or use a secrets manager with the following variables:

```bash
# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Database (use managed service in production)
SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/reddevil_analytics
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_secure_password

# Redis (use managed service in production)
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# API Keys (use secrets manager)
API_FOOTBALL_KEY=your_production_key
FOOTBALL_DATA_KEY=your_production_key
THESPORTSDB_KEY=your_production_key

# Security
ADMIN_API_KEY=generate_strong_random_key

# CORS (adjust for your frontend domain)
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_REDDEVIL=INFO
```

## Deployment Options

### Docker Deployment

#### Single Container

1. **Build the Docker image:**
```bash
docker build -t reddevil-backend:1.0.0 .
```

2. **Run the container:**
```bash
docker run -d \
  --name reddevil-backend \
  -p 8080:8080 \
  --env-file .env \
  --restart unless-stopped \
  reddevil-backend:1.0.0
```

3. **View logs:**
```bash
docker logs -f reddevil-backend
```

#### Docker Compose (with dependencies)

1. **Update docker-compose.yml for production:**
```yaml
version: '3.8'

services:
  app:
    image: reddevil-backend:1.0.0
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: prod
      # Add other production environment variables
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

2. **Deploy:**
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes Deployment

#### Prerequisites

- Kubernetes cluster (v1.24+)
- kubectl configured
- Helm (optional, recommended)

#### Basic Deployment

1. **Create namespace:**
```bash
kubectl create namespace reddevil-analytics
```

2. **Create secrets:**
```bash
kubectl create secret generic app-secrets \
  --from-literal=database-url=jdbc:postgresql://... \
  --from-literal=database-username=... \
  --from-literal=database-password=... \
  --from-literal=api-football-key=... \
  -n reddevil-analytics
```

3. **Create deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reddevil-backend
  namespace: reddevil-analytics
spec:
  replicas: 3
  selector:
    matchLabels:
      app: reddevil-backend
  template:
    metadata:
      labels:
        app: reddevil-backend
    spec:
      containers:
      - name: app
        image: reddevil-backend:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: database-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

4. **Create service.yaml:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: reddevil-backend-service
  namespace: reddevil-analytics
spec:
  selector:
    app: reddevil-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

5. **Deploy:**
```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

6. **Verify deployment:**
```bash
kubectl get pods -n reddevil-analytics
kubectl get services -n reddevil-analytics
```

### Cloud Platforms

#### AWS Deployment

**Option 1: AWS Elastic Beanstalk**

1. Install EB CLI:
```bash
pip install awsebcli
```

2. Initialize:
```bash
eb init -p docker reddevil-backend
```

3. Create environment:
```bash
eb create production-env
```

4. Deploy:
```bash
eb deploy
```

**Option 2: AWS ECS (Elastic Container Service)**

1. Create ECR repository:
```bash
aws ecr create-repository --repository-name reddevil-backend
```

2. Build and push image:
```bash
# Authenticate Docker to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and tag
docker build -t reddevil-backend .
docker tag reddevil-backend:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/reddevil-backend:latest

# Push
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/reddevil-backend:latest
```

3. Create ECS cluster, task definition, and service via AWS Console or CLI.

**Database & Cache:**
- Use AWS RDS for PostgreSQL
- Use AWS ElastiCache for Redis
- Store secrets in AWS Secrets Manager

#### Azure Deployment

**Azure App Service**

1. Create resource group:
```bash
az group create --name reddevil-rg --location eastus
```

2. Create App Service plan:
```bash
az appservice plan create \
  --name reddevil-plan \
  --resource-group reddevil-rg \
  --sku B2 \
  --is-linux
```

3. Create web app:
```bash
az webapp create \
  --resource-group reddevil-rg \
  --plan reddevil-plan \
  --name reddevil-backend \
  --deployment-container-image-name reddevil-backend:1.0.0
```

4. Configure environment variables:
```bash
az webapp config appsettings set \
  --resource-group reddevil-rg \
  --name reddevil-backend \
  --settings SPRING_PROFILES_ACTIVE=prod
```

**Database & Cache:**
- Use Azure Database for PostgreSQL
- Use Azure Cache for Redis
- Store secrets in Azure Key Vault

#### Google Cloud Platform

**Google Cloud Run**

1. Build and push to GCR:
```bash
gcloud builds submit --tag gcr.io/PROJECT_ID/reddevil-backend
```

2. Deploy to Cloud Run:
```bash
gcloud run deploy reddevil-backend \
  --image gcr.io/PROJECT_ID/reddevil-backend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

**Database & Cache:**
- Use Cloud SQL for PostgreSQL
- Use Cloud Memorystore for Redis
- Store secrets in Secret Manager

## Production Checklist

### Security

- [ ] Use HTTPS/TLS for all connections
- [ ] Store secrets in a secure vault (not in code or env files)
- [ ] Use strong, unique passwords
- [ ] Enable database SSL connections
- [ ] Configure proper CORS settings
- [ ] Implement rate limiting
- [ ] Regular security updates
- [ ] Enable firewall rules
- [ ] Use least privilege access
- [ ] Regular security audits

### Performance

- [ ] Configure connection pooling
- [ ] Enable caching (Redis)
- [ ] Optimize database queries
- [ ] Use CDN for static assets
- [ ] Enable compression
- [ ] Configure proper timeouts
- [ ] Load testing completed
- [ ] Auto-scaling configured

### Reliability

- [ ] Database backups configured
- [ ] Health checks enabled
- [ ] Circuit breakers configured
- [ ] Retry logic in place
- [ ] Multiple replicas/instances
- [ ] Graceful shutdown handling
- [ ] Disaster recovery plan
- [ ] High availability setup

### Monitoring

- [ ] Application metrics enabled
- [ ] Log aggregation configured
- [ ] Alerting rules set up
- [ ] Error tracking enabled
- [ ] Performance monitoring active
- [ ] Uptime monitoring
- [ ] Cost monitoring

### Documentation

- [ ] API documentation updated
- [ ] Deployment runbook created
- [ ] Troubleshooting guide available
- [ ] Architecture diagrams updated
- [ ] Incident response plan documented

## Monitoring and Logging

### Prometheus & Grafana

1. **Prometheus configuration (prometheus.yml):**
```yaml
scrape_configs:
  - job_name: 'reddevil-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8080']
```

2. **Import Grafana dashboard:**
- Use Spring Boot 2.1+ dashboard (ID: 11378)
- Or create custom dashboards

### ELK Stack

1. **Configure Logstash:**
```conf
input {
  file {
    path => "/app/logs/*.log"
    codec => json
  }
}
output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
  }
}
```

2. **Configure application logging:**
```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /app/logs/application.log
```

### Cloud-Native Monitoring

- **AWS**: CloudWatch Logs and Metrics
- **Azure**: Application Insights
- **GCP**: Cloud Logging and Monitoring

## Backup and Recovery

### Database Backups

**PostgreSQL:**
```bash
# Daily backup script
pg_dump -h your-db-host -U username reddevil_analytics | \
  gzip > backup_$(date +%Y%m%d).sql.gz

# Upload to S3/Azure/GCS
aws s3 cp backup_$(date +%Y%m%d).sql.gz s3://your-backup-bucket/
```

**Automated backups:**
- AWS RDS: Automated backups enabled (7-35 days retention)
- Azure Database: Automated backups (7-35 days)
- GCP Cloud SQL: Automated backups (7-365 days)

### Application State

- Cache data is ephemeral (Redis)
- Ensure all critical data is in PostgreSQL
- Document recovery procedures

### Recovery Testing

- Test backup restoration quarterly
- Document recovery time objectives (RTO)
- Document recovery point objectives (RPO)

## Troubleshooting

### Common Issues

**Application won't start:**
```bash
# Check logs
docker logs reddevil-backend
kubectl logs -n reddevil-analytics deployment/reddevil-backend

# Check health
curl http://localhost:8080/actuator/health
```

**Database connection issues:**
```bash
# Test connection
psql -h your-db-host -U username -d reddevil_analytics

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

**High memory usage:**
```bash
# Check metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Adjust JVM settings
JAVA_OPTS="-Xms512m -Xmx2g"
```

## Rolling Updates

### Zero-Downtime Deployment

1. Deploy new version alongside old
2. Health check new version
3. Gradually shift traffic
4. Monitor for errors
5. Complete cutover
6. Remove old version

### Rollback Procedure

```bash
# Kubernetes
kubectl rollout undo deployment/reddevil-backend -n reddevil-analytics

# Docker
docker stop reddevil-backend
docker rm reddevil-backend
docker run -d --name reddevil-backend reddevil-backend:previous-version
```

## Support

For deployment support:
- [GitHub Issues](https://github.com/Zephyrus-not-available/RedDevilAnalytics_Backend/issues)
- Check application logs: `/app/logs/`
- Review health endpoints: `/actuator/health`
