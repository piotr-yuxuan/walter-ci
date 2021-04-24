const core = require('@actions/core');
const github = require('@actions/github');
const {execFile} = require("child_process");

execFile('./resources/walter-ci.sh')
