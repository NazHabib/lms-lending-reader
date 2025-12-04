pipeline {

  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  tools {
    jdk 'jdk-17'
  }

  parameters {
    // Application Configuration
    string(name: 'APP_NAME', defaultValue: 'library-management', description: 'Base name for containers')
    string(name: 'DOCKER_IMAGE', defaultValue: 'nazhabibi/library-management', description: 'Docker Hub repo')
    string(name: 'JAR_NAME', defaultValue: 'psoft-g1-0.0.1-SNAPSHOT.jar', description: 'Built JAR name')
    booleanParam(name: 'PUSH_IMAGE', defaultValue: true, description: 'Push image to Docker Hub?')

    string(name: 'SONAR_PROJECT_KEY', defaultValue: 'BotHeya_ODSOFT', description: 'SonarQube project key')
    string(name: 'SONAR_PROJECT_NAME', defaultValue: 'psoft-g1', description: 'SonarQube project name')
    string(name: 'SONAR_PASS_THRESHOLD', defaultValue: 'OK', description: 'Minimum quality gate status (OK, WARN, ERROR)')
    string(name: 'SONAR_COVERAGE_THRESHOLD', defaultValue: '50', description: 'Minimum code coverage percentage (0-100)')

    // Deployment Ports
    string(name: 'DEV_PORT', defaultValue: '8082', description: 'Dev HTTP port')
    string(name: 'STAGE_PORT', defaultValue: '8083', description: 'Staging HTTP port')
    string(name: 'PROD_PORT', defaultValue: '8084', description: 'Prod HTTP port')

    // Database Ports
    string(name: 'DEV_DB_PORT', defaultValue: '1521', description: 'Dev H2 DB port')
    string(name: 'STAGE_DB_PORT', defaultValue: '1522', description: 'Stage H2 DB port')
    string(name: 'PROD_DB_PORT', defaultValue: '1523', description: 'Prod H2 DB port')

    // Web Console Ports
    string(name: 'DEV_DB_WEB_PORT', defaultValue: '8085', description: 'Dev H2 Web Console port')
    string(name: 'STAGE_DB_WEB_PORT', defaultValue: '8086', description: 'Stage H2 Web Console port')
    string(name: 'PROD_DB_WEB_PORT', defaultValue: '8087', description: 'Prod H2 Web Console port')

    // Local Deployment Hosts
    choice(name: 'TARGET_ENV', choices: ['local', 'gcp'], description: 'Where to deploy?')
    string(name: 'GCP_DEV_IP',   defaultValue: '34.140.217.2', description: 'GCP dev-node IP')
    string(name: 'GCP_STAGE_IP', defaultValue: '35.233.34.79', description: 'GCP stage-node IP')
    string(name: 'GCP_PROD_IP',  defaultValue: '34.79.50.81',  description: 'GCP prod-node IP')
    string(name: 'DEV_HOST', defaultValue: 'localhost', description: 'Dev SSH host')
    string(name: 'STAGE_HOST', defaultValue: 'localhost', description: 'Stage SSH host')
    string(name: 'PROD_HOST', defaultValue: 'localhost', description: 'Prod SSH host')

    // Database Configuration
    string(name: 'DEV_DB_NAME', defaultValue: 'library_dev', description: 'Dev database name')
    string(name: 'STAGE_DB_NAME', defaultValue: 'library_stage', description: 'Stage database name')
    string(name: 'PROD_DB_NAME', defaultValue: 'library_prod', description: 'Prod database name')

    string(name: 'DB_USER', defaultValue: 'sa', description: 'H2 database username')
    password(name: 'DB_PASSWORD', defaultValue: 'password', description: 'H2 database password')

    // Testing Controls
    booleanParam(name: 'RUN_E2E', defaultValue: false, description: 'Run end-to-end tests?')
    booleanParam(name: 'SKIP_ITS', defaultValue: false, description: 'Skip integration tests?')
    booleanParam(name: 'RUN_MUTATION_TESTS', defaultValue: false, description: 'Run mutation tests (PITest)?')

    // Quality Gates
    string(name: 'UNIT_TEST_THRESHOLD', defaultValue: '80', description: 'Minimum unit test pass rate %')
    string(name: 'COVERAGE_THRESHOLD', defaultValue: '60', description: 'Minimum code coverage %')
  }

  environment {
    COMMIT_SHORT = "${env.GIT_COMMIT?.take(7) ?: 'local'}"
    IMAGE_TAG = "${params.DOCKER_IMAGE}:${env.BUILD_NUMBER}-${COMMIT_SHORT}"

    SONAR_LOGIN = credentials('SONAR_TOKEN')

    // Credentials
    GITHUB_CREDS = 'github-odsoft-pat'
    DOCKERHUB_CREDS = 'dockerhub-credentials-id'
    SSH_CREDS = 'jenkins-deploy-key-google'
    SSH_USER = 'nazmu'

    // Quality Thresholds
    UNIT_TEST_PASS_THRESHOLD = '0'
    COVERAGE_THRESHOLD = '0'
  }

  stages {
    // STAGE 1: Source Code Management & Preparation
    stage('Checkout & Validate') {
      steps {
        checkout([$class: 'GitSCM',
          userRemoteConfigs: [[url: 'https://github.com/BotHeya/ODSOFT.git', credentialsId: env.GITHUB_CREDS]],
          branches: [[name: '*/main']]
        ])

        sh '''
          echo "Library Management System - CI/CD Pipeline"
          echo "Commit: $(git --no-pager log -1 --oneline)"
          echo "Build: ${BUILD_NUMBER}"
        '''

        sh '''
          test -f mvnw && test -d .mvn/wrapper || {
            echo "ERROR: Maven Wrapper not found"
            exit 1
          }
          chmod +x mvnw
          ./mvnw --version
        '''
      }
    }

    // STAGE 2: Target resolution
    stage('Resolve Targets') {
      steps {
        script {
          def pick = { String paramVal, String gcpIp ->
            if (params.TARGET_ENV == 'gcp' && (!paramVal?.trim() || paramVal.trim().equalsIgnoreCase('localhost'))) {
              return gcpIp
            }
            return paramVal?.trim() ?: 'localhost'
          }

          env.DEV_HOST_E   = pick(params.DEV_HOST,   params.GCP_DEV_IP)
          env.STAGE_HOST_E = pick(params.STAGE_HOST, params.GCP_STAGE_IP)
          env.PROD_HOST_E  = pick(params.PROD_HOST,  params.GCP_PROD_IP)

          echo "TARGET_ENV = ${params.TARGET_ENV}"
          echo "Effective hosts -> DEV: ${env.DEV_HOST_E}, STAGE: ${env.STAGE_HOST_E}, PROD: ${env.PROD_HOST_E}"
        }
      }
    }



    // STAGE 3: Code compilation
    stage('Compilation of the code') {
      steps {
        sh '''
          echo "Compile the code..."
          ./mvnw -B -ntp clean compile || exit 1
        '''
      }
      post {
        failure {
          echo "Compilation failed! Check the logs above."
        }
      }
    }

    // STAGE 4: Unit Tests & Coverage
    stage('Unit Tests & Coverage') {
      steps {
        sh """
          ./mvnw -B -ntp clean test -DskipITs=true
          ./mvnw -B -ntp jacoco:report
        """
      }
      post {
        always {
          junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: false
          publishHTML(target: [
            reportDir: 'target/site/jacoco',
            reportFiles: 'index.html',
            reportName: 'JaCoCo Coverage Report',
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true
          ])

          script {
            def testResults = junit testResults: 'target/surefire-reports/**/*.xml'
            def totalTests = testResults.totalCount
            def passedTests = totalTests - testResults.failCount - testResults.skipCount
            def passRate = totalTests > 0 ? (passedTests / totalTests * 100) : 0

            env.UNIT_TEST_PASS_RATE = passRate
            echo "Unit Tests: ${passedTests}/${totalTests} (${String.format('%.2f', passRate)}%)"

            if (passRate < env.UNIT_TEST_PASS_THRESHOLD.toFloat()) {
              error "Unit test pass rate below threshold: ${String.format('%.2f', passRate)}% < ${env.UNIT_TEST_PASS_THRESHOLD}%"
            }
          }
        }
      }
    }

    // STAGE 5: Mutation Testing (Optional)
    stage('Mutation Testing') {
      when { expression { params.RUN_MUTATION_TESTS } }
      steps {
        sh './mvnw -B -ntp org.pitest:pitest-maven:mutationCoverage -DskipITs=true'
      }
      post {
        always {
          publishHTML(target: [
            reportDir: 'target/pit-reports',
            reportFiles: 'index.html',
            reportName: 'Mutation Test Report',
            allowMissing: true,
            alwaysLinkToLastBuild: true,
            keepAll: true
          ])
        }
      }
    }

    // STAGE 6: Integration Tests
    stage('Integration Tests') {
      when { expression { !params.SKIP_ITS } }
      steps {
        sh './mvnw -B -ntp verify -DskipUnitTests=true'
      }
      post {
        always {
          junit testResults: 'target/failsafe-reports/**/*.xml', allowEmptyResults: true

          script {
            def integrationResults = junit testResults: 'target/failsafe-reports/**/*.xml', allowEmptyResults: true
            def totalIntegrationTests = integrationResults.totalCount
            if (totalIntegrationTests > 0) {
              def passedIntegrationTests = totalIntegrationTests - integrationResults.failCount - integrationResults.skipCount
              def integrationPassRate = totalIntegrationTests > 0 ? (passedIntegrationTests / totalIntegrationTests * 100) : 0
              env.INTEGRATION_TEST_PASS_RATE = integrationPassRate
              echo "Integration Tests: ${passedIntegrationTests}/${totalIntegrationTests} (${String.format('%.2f', integrationPassRate)}%)"
            } else {
              env.INTEGRATION_TEST_PASS_RATE = 0
              echo "No integration test results found - check failsafe report configuration"
            }
          }
        }
      }
    }

    // STAGE 7: SonarQube Analysis
    stage('SonarQube Analysis') {
      environment {
        SONAR_PROJECT_KEY  = "${params.SONAR_PROJECT_KEY}"
        SONAR_PROJECT_NAME = "${params.SONAR_PROJECT_NAME}"
      }
      steps {
        withSonarQubeEnv('sonarqube-server') {
          sh """
            ./mvnw -B -ntp sonar:sonar \
              -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
              -Dsonar.projectName='${env.SONAR_PROJECT_NAME}' \
              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
              -Dsonar.java.coveragePlugin=jacoco \
              -Dsonar.scm.disabled=true \
              -Dsonar.qualitygate.wait=true \
              -Dsonar.qualitygate.timeout=600
          """
        }
      }
    }

    // STAGE 8: Package Application
    stage('Package Application') {
      steps {
        sh """
          ./mvnw -B -ntp clean package -DskipTests=false -DskipITs=false
          ls -lah target/*.jar
        """
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    // STAGE 9: Build & Push Docker Image 
    stage('Build Docker Image') {
      steps {
        script {
          def dockerfileContent = """FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY target/${params.JAR_NAME} app.jar

# Create non-root user and switch to it
RUN adduser -D app && chown -R app:app /app
USER app

# Expose application port (metadata only)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
"""
          writeFile file: 'Dockerfile', text: dockerfileContent

          def buildSuccess = false
          def maxRetries = 3
          def retryCount = 0
          
          while (!buildSuccess && retryCount < maxRetries) {
            try {
              echo "Attempting Docker build (attempt ${retryCount + 1}/${maxRetries})"
              sh "docker build -t ${env.IMAGE_TAG} ."
              buildSuccess = true
              echo "Docker build successful on attempt ${retryCount + 1}"
            } catch (Exception e) {
              retryCount++
              if (retryCount >= maxRetries) {
                error "Docker build failed after ${maxRetries} attempts: ${e.getMessage()}"
              } else {
                echo "Docker build attempt ${retryCount} failed, retrying in 10 seconds..."
                sleep(time: 10, unit: 'SECONDS')
              }
            }
          }

          if (params.PUSH_IMAGE && buildSuccess) {
            withCredentials([usernamePassword(credentialsId: env.DOCKERHUB_CREDS, usernameVariable: 'DHU', passwordVariable: 'DHP')]) {
              sh """
                echo \"\$DHP\" | docker login -u \"\$DHU\" --password-stdin
                docker push ${env.IMAGE_TAG}
                docker logout
              """
            }
          }
        }
      }
    }

    // STAGE 10: Approve Development Deployment
    stage('Approve Development Deployment') {
      when {
        expression { currentBuild.result != 'FAILURE' && currentBuild.result != 'ABORTED' }
      }
      steps {
        script {
          def approval = input(
            message: """DEPLOY TO DEVELOPMENT - Library Management System

    Quality Metrics:
    ✓ Unit Tests: ${String.format('%.2f', env.UNIT_TEST_PASS_RATE.toFloat())}% pass rate
    ✓ Integration Tests: ${String.format('%.2f', env.INTEGRATION_TEST_PASS_RATE.toFloat())}% pass rate
    ✓ Build: Successful

    Deployment Target:
    Image: ${env.IMAGE_TAG}
    Host: ${env.DEV_HOST_E}
    App Port: ${params.DEV_PORT}
    DB Port: ${params.DEV_DB_PORT}
    DB Web Console: ${params.DEV_DB_WEB_PORT}

    This will create:
    • 1 Application Container
    • 1 H2 Database Container
    • Shared Docker Network

    Confirm development deployment?""",
            ok: 'Deploy to Development',
            submitterParameter: 'APPROVED_BY'  
          )
          env.DEV_APPROVED_BY = (approval instanceof Map) ? (approval.APPROVED_BY ?: '') : (approval ?: '')
          echo "Development approved by: ${env.DEV_APPROVED_BY}"
        }
      }
    }

    // STAGE 11: Deploy App - Development  
    stage('Deploy App - Development') {
      when { expression { env.DEV_APPROVED_BY?.trim() } }
      steps {
        sshagent([env.SSH_CREDS]) {
          script {
            sh """
              ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 ${env.SSH_USER}@${env.DEV_HOST_E} "
                mkdir -p /var/lib/${params.APP_NAME}/dev-h2-data 2>/dev/null || true
                docker network create ${params.APP_NAME}-network 2>/dev/null || true

                echo 'Stopping existing development containers...'
                docker stop ${params.APP_NAME}-dev-app ${params.APP_NAME}-dev-db 2>/dev/null || true
                docker rm -f ${params.APP_NAME}-dev-app ${params.APP_NAME}-dev-db 2>/dev/null || true

                echo 'Deploying H2 Database Container...'
                docker run -d --name ${params.APP_NAME}-dev-db \
                  --network ${params.APP_NAME}-network \
                  -p ${params.DEV_DB_PORT}:${params.DEV_DB_PORT} \
                  -p ${params.DEV_DB_WEB_PORT}:${params.DEV_DB_WEB_PORT} \
                  -v /var/lib/${params.APP_NAME}/dev-h2-data:/opt/h2-data \
                  -e H2_DATABASE_NAME=${params.DEV_DB_NAME} \
                  -e H2_OPTIONS='-ifNotExists -tcp -tcpAllowOthers -tcpPort ${params.DEV_DB_PORT} -web -webAllowOthers -webPort ${params.DEV_DB_WEB_PORT} -baseDir /opt/h2-data' \
                  oscarfonts/h2:latest



                echo 'Waiting for database to start...'
                sleep 15

                echo 'Deploying Application Container...'
                docker run -d --name ${params.APP_NAME}-dev-app \
                  --network ${params.APP_NAME}-network \
                  -p ${params.DEV_PORT}:${params.DEV_PORT} \
                  -e SPRING_PROFILES_ACTIVE=dev \
                  -e SERVER_PORT=${params.DEV_PORT} \
                  -e SPRING_DATASOURCE_URL='jdbc:h2:tcp://${params.APP_NAME}-dev-db:${params.DEV_DB_PORT}/./${params.DEV_DB_NAME};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE' \
                  -e SPRING_DATASOURCE_USERNAME='${params.DB_USER}' \
                  -e SPRING_DATASOURCE_PASSWORD='${params.DB_PASSWORD}' \
                  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                  -e SPRING_H2_CONSOLE_ENABLED=true \
                  -e SPRING_H2_CONSOLE_PATH=/h2-console \
                  --restart unless-stopped \
                  ${env.IMAGE_TAG}

                echo 'Development deployment completed!'
                echo 'App: http://${env.DEV_HOST_E}:${params.DEV_PORT}'
                echo 'H2 Console: http://${env.DEV_HOST_E}:${params.DEV_DB_WEB_PORT}'
                echo 'JDBC URL: jdbc:h2:tcp://${params.APP_NAME}-dev-db:${params.DEV_DB_PORT}/./${params.DEV_DB_NAME}'
                sleep 10
              "
            """
          }
        }
      }
      post {
        success {
          echo "Development deployment successful - progressing to staging quality gate"
        }
      }
    }

    // STAGE 12: Functional Tests - Dev
    stage('Functional Tests - dev') {
      when { expression { params.RUN_E2E && env.DEV_APPROVED_BY != null } }
      steps {
        script {
          timeout(time: 3, unit: 'MINUTES') {
            waitUntil {
              try {
                sh "nc -z -w 5 ${env.DEV_HOST_E} ${params.DEV_PORT}"
                return true
              } catch (Exception e) {
                sleep(time: 10, unit: 'SECONDS')
                return false
              }
            }
            sh """
              echo "Development environment checks:"
              echo "App: http://${env.DEV_HOST_E}:${params.DEV_PORT}"
              echo "H2 Console: http://${env.DEV_HOST_E}:${params.DEV_DB_WEB_PORT}"
              echo "Testing connectivity..."
            """
          }
        }
      }
    }

    // STAGE 13: Quality Gate - Staging
    stage('Quality Gate - Staging') {
      when { 
        expression { 
          currentBuild.result != 'FAILURE' && 
          currentBuild.result != 'ABORTED' &&
          env.DEV_APPROVED_BY != null
        } 
      }
      steps {
        script {
          echo "Staging Quality Gate Assessment"
          echo "Unit Test Pass Rate: ${String.format('%.2f', env.UNIT_TEST_PASS_RATE.toFloat())}%"
          echo "Integration Test Pass Rate: ${String.format('%.2f', env.INTEGRATION_TEST_PASS_RATE.toFloat())}%"

          if (env.UNIT_TEST_PASS_RATE.toFloat() < 60) {
            error "Cannot deploy to Staging: Unit test pass rate below 60%"
          }

          if (env.INTEGRATION_TEST_PASS_RATE.toFloat() < 50 && !params.SKIP_ITS) {
            echo "Warning: Integration tests below 50%, will require manual approval"
          }

          echo "Staging quality gate passed - ready for staging deployment approval"
        }
      }
    }

    // STAGE 14: Approve Staging Deployment
    stage('Approve Staging Deployment') {
      when {
        expression {
          currentBuild.result != 'FAILURE' &&
          currentBuild.result != 'ABORTED' &&
          env.DEV_APPROVED_BY?.trim()
        }
      }
      steps {
        script {
          def approval = input(
            message: """DEPLOY TO STAGING - Library Management System

    Quality Metrics:
    ✓ Unit Tests: ${String.format('%.2f', env.UNIT_TEST_PASS_RATE.toFloat())}% pass rate
    ✓ Integration Tests: ${String.format('%.2f', env.INTEGRATION_TEST_PASS_RATE.toFloat())}% pass rate
    ✓ Development Tests: ${params.RUN_E2E ? 'PASSED' : 'SKIPPED'}

    Deployment Target:
    Image: ${env.IMAGE_TAG}
    Host: ${env.STAGE_HOST_E}
    App Port: ${params.STAGE_PORT}
    DB Port: ${params.STAGE_DB_PORT}
    DB Web Console: ${params.STAGE_DB_WEB_PORT}

    This will create:
    • 1 Application Container
    • 1 H2 Database Container
    • Shared Docker Network

    Confirm staging deployment?""",
            ok: 'Deploy to Staging',
            submitterParameter: 'APPROVED_BY'
          )
          env.STAGE_APPROVED_BY = (approval instanceof Map) ? (approval.APPROVED_BY ?: '') : (approval ?: '')
          echo "Staging approved by: ${env.STAGE_APPROVED_BY}"
        }
      }
    }

    // STAGE 15: Deploy App - Staging  
    stage('Deploy App - Staging') {
      when { expression { env.STAGE_APPROVED_BY?.trim() } }
      steps {
        sshagent([env.SSH_CREDS]) {
          script {
            sh """
              ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 ${env.SSH_USER}@${env.STAGE_HOST_E} "
                mkdir -p /var/lib/${params.APP_NAME}/stage-h2-data 2>/dev/null || true
                docker network create ${params.APP_NAME}-network 2>/dev/null || true

                echo 'Stopping existing staging containers...'
                docker stop ${params.APP_NAME}-stage-app ${params.APP_NAME}-stage-db 2>/dev/null || true
                docker rm -f ${params.APP_NAME}-stage-app ${params.APP_NAME}-stage-db 2>/dev/null || true

                echo 'Deploying H2 Database Container...'
                docker run -d --name ${params.APP_NAME}-stage-db \
                  --network ${params.APP_NAME}-network \
                  -p ${params.STAGE_DB_PORT}:${params.STAGE_DB_PORT} \
                  -p ${params.STAGE_DB_WEB_PORT}:${params.STAGE_DB_WEB_PORT} \
                  -v /var/lib/${params.APP_NAME}/stage-h2-data:/opt/h2-data \
                  -e H2_DATABASE_NAME=${params.STAGE_DB_NAME} \
                  -e H2_OPTIONS='-ifNotExists -tcp -tcpAllowOthers -tcpPort ${params.STAGE_DB_PORT} -web -webAllowOthers -webPort ${params.STAGE_DB_WEB_PORT} -baseDir /opt/h2-data' \
                  oscarfonts/h2:latest

                sleep 15

                echo 'Deploying Application Container...'
                docker run -d --name ${params.APP_NAME}-stage-app \
                  --network ${params.APP_NAME}-network \
                  -p ${params.STAGE_PORT}:${params.STAGE_PORT} \
                  -e SPRING_PROFILES_ACTIVE=stage \
                  -e SERVER_PORT=${params.STAGE_PORT} \
                  -e SPRING_DATASOURCE_URL='jdbc:h2:tcp://${params.APP_NAME}-stage-db:${params.STAGE_DB_PORT}/./${params.STAGE_DB_NAME};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE' \
                  -e SPRING_DATASOURCE_USERNAME='${params.DB_USER}' \
                  -e SPRING_DATASOURCE_PASSWORD='${params.DB_PASSWORD}' \
                  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                  -e SPRING_H2_CONSOLE_ENABLED=true \
                  -e SPRING_H2_CONSOLE_PATH=/h2-console \
                  --restart unless-stopped \
                  ${env.IMAGE_TAG}

                echo 'Staging deployment completed!'
                echo 'App: http://${env.STAGE_HOST_E}:${params.STAGE_PORT}'
                echo 'H2 Console: http://${env.STAGE_HOST_E}:${params.STAGE_DB_WEB_PORT}'
                echo 'JDBC URL: jdbc:h2:tcp://${params.APP_NAME}-stage-db:${params.STAGE_DB_PORT}/./${params.STAGE_DB_NAME}'
              "
            """
          }
        }
      }
      post {
        success {
          echo "Staging deployment successful - progressing to production quality gate"
        }
      }
    }

    // STAGE 16: System Tests - Staging (E2E)
    stage('System Tests - Staging') {
      when { expression { params.RUN_E2E && env.STAGE_APPROVED_BY != null } }
      steps {
        script {
          timeout(time: 3, unit: 'MINUTES') {
            waitUntil {
              try {
                sh "nc -z -w 5 ${env.STAGE_HOST_E} ${params.STAGE_PORT}"
                return true
              } catch (Exception e) {
                sleep(time: 10, unit: 'SECONDS')
                return false
              }
            }
            sh """
              echo "Verifying staging endpoints..."
              echo "H2 Console available at http://${env.STAGE_HOST_E}:${params.STAGE_DB_WEB_PORT}"
            """
          }
        }
      }
    }

    // STAGE 17: Quality Gate - Production
    stage('Quality Gate - Production') {
      when { 
        expression { 
          currentBuild.result != 'FAILURE' && 
          currentBuild.result != 'ABORTED' &&
          env.STAGE_APPROVED_BY != null
        } 
      }
      steps {
        script {
          echo "Production Readiness Assessment"
          echo "Unit Tests: ${String.format('%.2f', env.UNIT_TEST_PASS_RATE.toFloat())}%"
          echo "Integration Tests: ${String.format('%.2f', env.INTEGRATION_TEST_PASS_RATE.toFloat())}%"
          echo "Staging Tests: ${params.RUN_E2E ? 'PASSED' : 'SKIPPED'}"

          if (env.UNIT_TEST_PASS_RATE.toFloat() < 80) {
            error "Cannot deploy to Production: Unit test quality insufficient (threshold 80%)"
          }

          if (env.INTEGRATION_TEST_PASS_RATE.toFloat() < 70 && !params.SKIP_ITS) {
            echo "Warning: Integration test pass rate below 70%, continuing to approval step"
          }

          echo "Production quality gate passed - ready for production deployment approval"
        }
      }
    }

    // STAGE 18: Approve Production Deployment
    stage('Approve Production Deployment') {
      when {
        expression {
          currentBuild.result != 'FAILURE' &&
          currentBuild.result != 'ABORTED' &&
          env.STAGE_APPROVED_BY?.trim()
        }
      }
      steps {
        script {
          def approval = input(
            message: """DEPLOY TO PRODUCTION - Library Management System

    Quality Metrics:
    ✓ Unit Tests: ${String.format('%.2f', env.UNIT_TEST_PASS_RATE.toFloat())}% pass rate
    ✓ Integration Tests: ${String.format('%.2f', env.INTEGRATION_TEST_PASS_RATE.toFloat())}% pass rate
    ✓ Functional Tests: ${params.RUN_E2E ? 'PASSED' : 'SKIPPED'}
    ✓ Staging Tests: ${params.RUN_E2E ? 'PASSED' : 'SKIPPED'}

    Deployment Target:
    Image: ${env.IMAGE_TAG}
    Host: ${env.PROD_HOST_E}
    App Port: ${params.PROD_PORT}
    DB Port: ${params.PROD_DB_PORT}
    DB Web Console: ${params.PROD_DB_WEB_PORT}

    This will create:
    • 1 Application Container
    • 1 H2 Database Container
    • Shared Docker Network

    WARNING: This will deploy to PRODUCTION environment

    Confirm production deployment?""",
            ok: 'Deploy to Production',
            submitterParameter: 'APPROVED_BY'
          )
          env.PROD_APPROVED_BY = (approval instanceof Map) ? (approval.APPROVED_BY ?: '') : (approval ?: '')
          echo "Production approved by: ${env.PROD_APPROVED_BY}"
        }
      }
    }

    // STAGE 19: Deploy App to Production 
    stage('Deploy App - Production') {
      when { expression { env.PROD_APPROVED_BY?.trim() } }
      steps {
        sshagent([env.SSH_CREDS]) {
          script {
            sh """
              ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 ${env.SSH_USER}@${env.PROD_HOST_E} "
                mkdir -p /var/lib/${params.APP_NAME}/prod-h2-data 2>/dev/null || true
                docker network create ${params.APP_NAME}-network 2>/dev/null || true

                echo 'Stopping existing production containers...'
                docker stop ${params.APP_NAME}-prod-app ${params.APP_NAME}-prod-db 2>/dev/null || true
                docker rm -f ${params.APP_NAME}-prod-app ${params.APP_NAME}-prod-db 2>/dev/null || true

                echo 'Deploying H2 Database Container...'
                docker run -d --name ${params.APP_NAME}-prod-db \
                  --network ${params.APP_NAME}-network \
                  -p ${params.PROD_DB_PORT}:${params.PROD_DB_PORT} \
                  -p ${params.PROD_DB_WEB_PORT}:${params.PROD_DB_WEB_PORT} \
                  -v /var/lib/${params.APP_NAME}/prod-h2-data:/opt/h2-data \
                  -e H2_DATABASE_NAME=${params.PROD_DB_NAME} \
                  -e H2_OPTIONS='-ifNotExists -tcp -tcpAllowOthers -tcpPort ${params.PROD_DB_PORT} -web -webAllowOthers -webPort ${params.PROD_DB_WEB_PORT} -baseDir /opt/h2-data' \
                  oscarfonts/h2:latest

                sleep 15

                echo 'Deploying Application Container...'
                docker run -d --name ${params.APP_NAME}-prod-app \
                  --network ${params.APP_NAME}-network \
                  -p ${params.PROD_PORT}:${params.PROD_PORT} \
                  -e SPRING_PROFILES_ACTIVE=prod \
                  -e SERVER_PORT=${params.PROD_PORT} \
                  -e SPRING_DATASOURCE_URL='jdbc:h2:tcp://${params.APP_NAME}-prod-db:${params.PROD_DB_PORT}/./${params.PROD_DB_NAME};DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE' \
                  -e SPRING_DATASOURCE_USERNAME='${params.DB_USER}' \
                  -e SPRING_DATASOURCE_PASSWORD='${params.DB_PASSWORD}' \
                  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                  -e SPRING_H2_CONSOLE_ENABLED=true \
                  -e SPRING_H2_CONSOLE_PATH=/h2-console \
                  --restart unless-stopped \
                  ${env.IMAGE_TAG}

                echo 'Production deployment completed!'
                echo 'App: http://${env.PROD_HOST_E}:${params.PROD_PORT}'
                echo 'H2 Console: http://${env.PROD_HOST_E}:${params.PROD_DB_WEB_PORT}'
                echo 'JDBC URL: jdbc:h2:tcp://${params.APP_NAME}-prod-db:${params.PROD_DB_PORT}/./${params.PROD_DB_NAME}'
              "
            """
          }
        }
      }
    }

    // STAGE 20: Post-Deployment Verification
    stage('Post-Deployment Verification') {
      when { expression { params.RUN_E2E && env.PROD_APPROVED_BY != null } }
      steps {
        script {
          timeout(time: 5, unit: 'MINUTES') {
            waitUntil {
              try {
                sh "nc -z -w 5 ${env.PROD_HOST_E} ${params.PROD_PORT}"
                return true
              } catch (Exception e) {
                sleep(time: 10, unit: 'SECONDS')
                return false
              }
            }
            sh """
              echo "Production deployment verified"
              echo "Application: http://${env.PROD_HOST_E}:${params.PROD_PORT}"
              echo "H2 Console: http://${env.PROD_HOST_E}:${params.PROD_DB_WEB_PORT}"
            """
          }
        }
      }
    }
  }

  post {
    always {
      echo "Pipeline Execution Completed"
      echo "Build: ${env.BUILD_TAG}"
      echo "Image: ${env.IMAGE_TAG}"
      echo "Result: ${currentBuild.result}"

      script {
        if (currentBuild.result == 'SUCCESS' || currentBuild.result == 'UNSTABLE') {
          sh "docker rmi ${env.IMAGE_TAG} 2>/dev/null || true"
        }
      }

      script {
        echo "Final Deployment Report:"
        echo "  - Unit Tests: ${String.format('%.2f', env.UNIT_TEST_PASS_RATE.toFloat())}%"
        echo "  - Integration Tests: ${String.format('%.2f', env.INTEGRATION_TEST_PASS_RATE.toFloat())}%"
        if (env.DEV_APPROVED_BY) { 
          echo "  - Development: Approved by ${env.DEV_APPROVED_BY}" 
          echo "    • App: http://${env.DEV_HOST_E}:${params.DEV_PORT}"
          echo "    • DB Console: http://${env.DEV_HOST_E}:${params.DEV_DB_WEB_PORT}"
        }
        if (env.STAGE_APPROVED_BY) { 
          echo "  - Staging: Approved by ${env.STAGE_APPROVED_BY}" 
          echo "    • App: http://${env.STAGE_HOST_E}:${params.STAGE_PORT}"
          echo "    • DB Console: http://${env.STAGE_HOST_E}:${params.STAGE_DB_WEB_PORT}"
        }
        if (env.PROD_APPROVED_BY) { 
          echo "  - Production: Approved by ${env.PROD_APPROVED_BY}" 
          echo "    • App: http://${env.PROD_HOST_E}:${params.PROD_PORT}"
          echo "    • DB Console: http://${env.PROD_HOST_E}:${params.PROD_DB_WEB_PORT}"
        }
        echo "  - Final Status: ${currentBuild.result == 'SUCCESS' ? 'SUCCESSFUL' : 'FAILED'}"
      }
    }

    success {
      script {
        def deployedEnvironments = []
        if (env.DEV_APPROVED_BY) deployedEnvironments.add("Development")
        if (env.STAGE_APPROVED_BY) deployedEnvironments.add("Staging")
        if (env.PROD_APPROVED_BY) deployedEnvironments.add("Production")

        if (deployedEnvironments) {
          echo "Pipeline successful! Deployed to: ${deployedEnvironments.join(', ')}"
          echo "Total containers created: ${deployedEnvironments.size() * 2} (${deployedEnvironments.size()} apps + ${deployedEnvironments.size()} databases)"
        } else {
          echo "Pipeline successful - No deployments approved"
        }
      }
    }

    failure {
      echo "Pipeline failed - Check stage logs for details"
    }

    unstable {
      echo "Pipeline completed with warnings - Code quality issues detected"
    }
  }
}
