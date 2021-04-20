sudo apt-get install -y clojure
clojure -Sdeps '{:aliases {:walter-ci {:replace-deps {com.github.piotr-yuxuan/walter-ci {:mvn/version "LATEST"}}}}}' -M:walter-ci -m com.piotr-yuxuan.walter-ci.main
