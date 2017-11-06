# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

iptables -P INPUT ACCEPT
iptables -P FORWARD ACCEPT
iptables -P OUTPUT ACCEPT
iptables -A INPUT -i lo -j ACCEPT 
iptables -A INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8081 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8446 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8082 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8447 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8083 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8450 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8444 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8443 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8084 -j ACCEPT 
iptables -A INPUT -p tcp -m state --state NEW -m tcp --dport 8449 -j ACCEPT 
iptables -A INPUT -p tcp -m tcp --dport 3306 -j ACCEPT 
iptables -A INPUT -p tcp -m tcp --dport 1812 -j ACCEPT 
iptables -A INPUT -j DROP 
iptables -A OUTPUT -o lo -j ACCEPT 
