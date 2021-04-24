const core = require('@actions/core');
const github = require('@actions/github');
const {execSync, execFileSync} = require("child_process");

execSync('pwd')
execSync('ls -hal')

