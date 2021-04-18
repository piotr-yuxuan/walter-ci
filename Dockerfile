FROM clojure:bbacdda3e08cae89421b5a2f1958389177d65cff
COPY walter-ci.sh /walter-ci.sh
ENTRYPOINT ["/walter-ci.sh"]
