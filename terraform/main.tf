terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.82.0"
    }
  }
  cloud {
    organization = "codepawfect"
    workspaces {
      name = "animal-welfare"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "animal-welfare-rg" {
  name     = "animal-welfare-rg"
  location = "westeurope"
}

resource "azurerm_service_plan" "animal-welfare-service-plan" {
  name                = "animal-welfare-app-service-plan"
  location            = azurerm_resource_group.animal-welfare-rg.location
  resource_group_name = azurerm_resource_group.animal-welfare-rg.name
  sku_name            = "F1"
  os_type             = "Linux"
}

resource "azurerm_linux_web_app" "example" {
  name                = "animal-welfare-web-app"
  resource_group_name = azurerm_resource_group.animal-welfare-rg.name
  location            = azurerm_service_plan.animal-welfare-service-plan.location
  service_plan_id     = azurerm_service_plan.animal-welfare-service-plan.id

  site_config {}
  app_settings = {
    "SPRING_R2DBC_URL"      = "<url>",
    "SPRING_R2DBC_USERNAME" = "<username>",
    "SPRING_R2DBC_PASSWORD" = "<password>",
    "MANAGEMENT_USERNAME"   = "<username>",
    "MANAGEMENT_PASSWORD"   = "<password>",
    "JWT_SECRET"            = "<secret>"
  }
}