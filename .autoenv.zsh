endpoints()
{
  if [ -f "build.sc" ]; then
    if [[ $# == "1" ]]; then
      mill -w Endpoints.run
    else
      mill Endpoints.run
    fi
  else
    echo "No build.sc found";
  fi
}
