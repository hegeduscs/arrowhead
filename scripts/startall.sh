# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

/etc/init.d/arrowhead.api start
/etc/init.d/arrowhead.authorization start
/etc/init.d/arrowhead.gatekeeper start
/etc/init.d/arrowhead.orchestrator start
/etc/init.d/arrowhead.serviceregistry start
