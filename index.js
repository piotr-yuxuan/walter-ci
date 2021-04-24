const core = require('@actions/core');
const github = require('@actions/github');
const {execFileSync} = require("child_process");

execFileSync('./resources/walter-ci.sh')
