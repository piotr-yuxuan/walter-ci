const core = require('@actions/core');
const github = require('@actions/github');
const {execSync} = require("child_process");

function loggedExecSync(commandString) {
    console.log(`::group::$ ${commandString}`)
    try {
        execSync(commandString)
    } catch (error) {
        console.log(`error: ${error.message}`);
    }
    console.log('::endgroup::')
}

loggedExecSync("pwd")
loggedExecSync("id")
loggedExecSync("ls -hal")
loggedExecSync("lein uberjar")
loggedExecSync("ls -hal target")
loggedExecSync("echo 'blah blah' > blah.txt")
loggedExecSync("cat blah.txt")
loggedExecSync("rm blah.txt")
//loggedExecSync("docker build -t piotryuxuan/walter-ci:latest .")
