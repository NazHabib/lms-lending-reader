Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/jammy64"
  config.vm.hostname = "ci-agent"
  config.vm.boot_timeout = 600

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.cpus = 2
    vb.gui = true
    vb.customize ["modifyvm", :id, "--uart1", "off"]
  end

  # Application Ports (Dev / Stage / Prod)
  config.vm.network "forwarded_port", guest: 8182, host: 8182  # Stage 1 (Dev)
  config.vm.network "forwarded_port", guest: 8183, host: 8183  # Stage 2 (Stage)
  config.vm.network "forwarded_port", guest: 8184, host: 8184  # Stage 3 (Prod)

  # H2 TCP (Dev / Stage / Prod)
  config.vm.network "forwarded_port", guest: 1621, host: 1621  # DB Dev
  config.vm.network "forwarded_port", guest: 1622, host: 1622  # DB Stage
  config.vm.network "forwarded_port", guest: 1623, host: 1623  # DB Prod

  # H2 Web Console (Dev / Stage / Prod)
  config.vm.network "forwarded_port", guest: 8285, host: 8285  # H2 Dev Web
  config.vm.network "forwarded_port", guest: 8286, host: 8286  # H2 Stage Web
  config.vm.network "forwarded_port", guest: 8287, host: 8287  # H2 Prod Web

  config.vm.provision "ansible_local" do |ansible|
    ansible.playbook = "provision.yml"
  end
end
