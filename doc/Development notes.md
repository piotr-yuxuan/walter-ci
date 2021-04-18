* Development notes

It takes time to build the image every time. On walter-ci, every push
should create a new Docker image and push it somewhere so that we can
prepare the entrypoint.

We can run a (private) action to update the Docker image version every
month.

Instead of a shell script we can just copy a jar, and put all Walter
logic in this jar.

We keep the checkout as an external action to be explicit, but
anything else will be handled inside Walter.
