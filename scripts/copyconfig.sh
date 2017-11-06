for service in api authorization gatekeeper orchestrator serviceregistry
do
	cp /home/$service/config/* /home/plosz/arrowhead/$service/config
done

exit 0
