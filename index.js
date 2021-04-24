const core = require('@actions/core');
const github = require('@actions/github');
const {exec} = require("child_process");

function loggedExec(commandString) {
    exec(commandString, (error, stdout, stderr) => {
        console.log(`::group::$ ${commandString}`)
        if (error) {
            console.log(`error: ${error.message}`);
            return;
        }
        if (stderr) {
            console.log(`stderr: ${stderr}`);
            return;
        }
        console.log(`stdout: ${stdout}`);
        console.log('::endgroup::')
    });
}

loggedExec("pwd")
loggedExec("id")
loggedExec("ls -hal")
loggedExec("mkdir target")
//loggedExec("lein uberjar")
loggedExec("ls -hal target")
loggedExec("echo 'blah blah' > blah.txt")
loggedExec("cat blah.txt")
loggedExec("rm blah.txt")
//loggedExec("docker build -t piotryuxuan/walter-ci:latest .")
