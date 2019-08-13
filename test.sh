#!/usr/bin/env bash

mvn clean verify surefire-report:report site -DgenerateReports=false
echo -e "\033[0;31mPlease open following file in web browser => \033[1;32mtarget/site/surefire-report.html"