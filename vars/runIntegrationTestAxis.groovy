#!/usr/bin/env groovy

import groovy.transform.Field

@Field Map ymlFiles = [
  'nodejs': 'tests/versions/nodejs.yml',
  'python': 'tests/versions/python.yml',
  'ruby': 'tests/versions/ruby.yml',
  'server': 'tests/versions/apm_server.yml'
]

@Field Map agentEnvVar = [
  'go': 'APM_AGENT_GO_PKG',
  'java': 'APM_AGENT_JAVA_PKG',
  'nodejs': 'APM_AGENT_NODEJS_PKG',
  'python': 'APM_AGENT_PYTHON_PKG',
  'ruby': 'APM_AGENT_RUBY_PKG',
  'server': 'APM_SERVER_BRANCH'
]

@Field Map agentYamlVar = [
  'go': 'GO_AGENT',
  'java': 'JAVA_AGENT',
  'nodejs': 'NODEJS_AGENT',
  'python': 'PYTHON_AGENT',
  'ruby': 'RUBY_AGENT',
  'server': 'APM_SERVER'
]
  
def call(agentType){
  def parallelStages = [:]
  def nodeVersions = readYaml(file: ymlFiles[agentType])
  def elasticStackVersions = readYaml(file: ymlFiles["server"])
  def serverKey = agentYamlVar["server"]
  def agentKey = agentYamlVar[agentType]
  
  def elasticStackVersNoExcluded = elasticStackVersions[serverKey]?.findAll{!elasticStackVersions?.exclude?.contains(it)}
  def nodeVersNoExcluded = nodeVersions[agentKey]?.findAll{!nodeVersions?.exclude?.contains(it)}
  
  elasticStackVersNoExcluded.each{ server ->
    nodeVersNoExcluded.each{ agent ->
      def tag = "${agentType} ${agent}-ES:${ELASTIC_STACK_VERSION}-APM:${server}"
      parallelStages[tag] = nodeIntegrationTest(tag, agent, server, "${agentType}")
    }
  }
  parallel(parallelStages)
}

def nodeIntegrationTest(tag, agent, server, agentType){
  return {
//    node('linux') {
      build(
        job: 'apm-integration-testing-pipeline', 
        parameters: [
          string(name: 'JOB_SHELL', value: "${JOB_SHELL}"), 
          string(name: 'JOB_INTEGRATION_TEST_BRANCH_SPEC', value: "${JOB_INTEGRATION_TEST_BRANCH_SPEC}"), 
          string(name: 'ELASTIC_STACK_VERSION', value: "${ELASTIC_STACK_VERSION}"), 
          string(name: 'APM_SERVER_BRANCH', value: server), /** TODO process VERSION;--release */ 
          string(name: agentEnvVar[agentType], value: agent), 
          string(name: 'BUILD_OPTS', value: ''), /** TODO process VERSION;--release */ 
          booleanParam(name: "${agentType}_Test", value: true)], 
          wait: true,
          propagate: true)
//    }
  }  
}