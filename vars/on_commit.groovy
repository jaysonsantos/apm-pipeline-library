/*
  Copyright © 2018 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

/**
  Execute some block of code if the built was trigger by a commit on the repo.
  It requires to initialise the pipeline with github_enterprise_constructor() first.
*/
void call(Map args = [:], body){

  // do nothing if not commit
  if (!env.GIT_BUILD_CAUSE.equals("commit")){ 
    return
  }
  
  def branch = env.BRANCH_NAME
    
  // do nothing if branch doesn't match regex
  if (args.to){
    if (!(branch ==~ args.to)){
      return
    }
  }
  
  echo "running because of a commit to ${branch}"
  body()
  
}
