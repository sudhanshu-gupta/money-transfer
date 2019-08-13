#!/usr/bin/env bash
set -e

./mvnw clean verify surefire-report:report site -DgenerateReports=false

URL=file://$(pwd)/target/site/surefire-report.html
if [[ "$OSTYPE" == "linux-gnu" ]]; then
   [[ -x $BROWSER ]] && exec "$BROWSER" "$URL"
   path=$(which xdg-open || which gnome-open) && exec "$path" "$URL"
elif [[ "$OSTYPE" == "darwin"* ]]; then
   open "$URL"
elif [[ "$OSTYPE" == "cygwin" ]]; then
   [[ -x $BROWSER ]] && exec "$BROWSER" "$URL"
   path=$(which xdg-open || which gnome-open) && exec "$path" "$URL"
elif [[ "$OSTYPE" == "msys" ]]; then
   [[ -x $BROWSER ]] && exec "$BROWSER" "$URL"
   path=$(which xdg-open || which gnome-open) && exec "$path" "$URL"
else
  echo "Can't find browser"
  echo -e "\033[0;31mPlease open following file in web browser => \033[1;32m$URL"
fi