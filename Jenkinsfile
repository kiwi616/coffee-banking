pipeline {
	agent {
		docker {
			image 'openjdk:8u181-jdk' //java 9 could be used if sdkmanager got an update
		}
	}
	environment {
		IS_RELEASE="true"
		ANDROID_HOME="$WORKSPACE/android-sdk"
	}
	options {
		timeout(time: 2, unit: 'HOURS')
		buildDiscarder(
			logRotator(
				artifactDaysToKeepStr: env.BRANCH_NAME == 'master' || env.BRANCH_NAME.contains('hotfix') || env.BRANCH_NAME.contains('release')
				? '' : '14',
				artifactNumToKeepStr: '',
				// days/number to keep build job information
				daysToKeepStr: env.BRANCH_NAME == 'master' || env.BRANCH_NAME.contains('hotfix') || env.BRANCH_NAME.contains('release')
				? '' : '14',
				numToKeepStr: ''
			)
	    )
	}
	stages{	
		stage("Preparing System"){
			steps{
				sh 'wget https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip'
				sh 'unzip sdk-tools-linux-4333796.zip -d android-sdk'
				sh 'java -version'
				sh 'javac -version'
				dir ('android-sdk/tools/bin') {
                    sh 'yes | ./sdkmanager --licenses'
                    sh './sdkmanager --update'
                    sh './sdkmanager --list'
                    sh 'yes | ./sdkmanager --licenses'
                }
			}
		}
		stage("Build"){
			steps{
			    sh 'chmod 777 gradlew'
				sh './gradlew clean assembleRelease buildRelease bundleRelease'
			}
		}
		stage("Archive APK"){
			steps{
                archiveArtifacts artifacts: "app/build/outputs/apk/release/*.apk, " +
                        "app/build/outputs/bundle/release/*.aab", fingerprint: true
			}
		}
	}
}
