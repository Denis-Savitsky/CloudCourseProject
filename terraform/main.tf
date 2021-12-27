terraform {
    required_version = ">= 1.0.0"

    required_providers {
        rustack = {
            source  = "pilat/rustack"
            version = "0.1.9"
        }
    }
}

provider "rustack" {
    api_endpoint = "https://cloud.mephi.ru"
    token = "3ed3e5598eac32389f839e9d8560997938eb1554"
}

# Получение проекта
data "rustack_project" "my_project" {
    name = "Мой проект"
}

# Получение доступного гипервизора KVM
data "rustack_hypervisor" "kvm" {
    project_id = data.rustack_project.my_project.id
    name = "KVM"
}

# Создание ВЦОД KVM
resource "rustack_vdc" "vdc1" {
    name = "KVM Terraform1"
    project_id = data.rustack_project.my_project.id
    hypervisor_id = data.rustack_hypervisor.kvm.id
}

# Получение автоматически созданной при создании ВЦОД сервисной сети
data "rustack_network" "service_network" {
    vdc_id = resource.rustack_vdc.vdc1.id
    name = "Сеть"
}

# Получение доступного типа дисков
data "rustack_storage_profile" "ocfs2" {
    vdc_id = resource.rustack_vdc.vdc1.id
    name = "ocfs2"
}

# Получение доступного шаблона ОС
data "rustack_template" "docker20" {
    vdc_id = resource.rustack_vdc.vdc1.id
    name = "Docker 20.10 (Ubuntu 20.04)"
}

# Получение доступного шаблона брандмауера
data "rustack_firewall_template" "allow_default" {
    vdc_id = resource.rustack_vdc.vdc1.id
    name = "По-умолчанию"
}


data "rustack_firewall_template" "allow_all" {
    vdc_id = resource.rustack_vdc.vdc1.id
    name = "Разрешить входящий трафик"
}

# Создание сервера
resource "rustack_vm" "vm" {
    vdc_id = resource.rustack_vdc.vdc1.id
    name = "Server"
    cpu = 3
    ram = 3

    template_id = data.rustack_template.docker20.id
    
    user_data = "${file("user_data.yaml")}"

    system_disk = "20-ocfs2"

    port {
        network_id = data.rustack_network.service_network.id
        firewall_templates = [
            data.rustack_firewall_template.allow_default.id,
            data.rustack_firewall_template.allow_all.id
        ]
    }

    floating = true
}