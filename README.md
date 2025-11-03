[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/cousPqCv)

# Tartan

## Building

The build instructions can be found [here](./docs/build_instructions.md).

## Deployment and Reversion

### Automated Deployment

Deployment is handled by `.github/workflows/deploy.yml` and is completely automated:
- When a new commit is pushed to main (via a PR merge), the workflow automatically:
  1. Builds and tests the code
  2. Tags the current running version as `:prev` (for rollback)
  3. Builds new Docker containers
  4. Deploys the new version to Cybera
  
The deployment process ensures that the previous version is always preserved for quick rollback.

### Rollback to Previous Version

There are **three methods** to rollback to the previous version:

#### Method 1: Using the Rollback Script (Recommended - Single Command)

Navigate to the `smart-home` directory and run:

```bash
cd ~/prod/smart-home
./rollback.sh
```

This script will:
- Verify that previous versions exist
- Tag previous versions as active
- Restart the backend services with the previous version
- Display verification commands

#### Method 2: GitHub Actions Workflow

1. Navigate to the `Actions` tab of the repository
2. Select `Revert Backend` from the left workflow panel
3. Click `Run workflow` from the blue box in the centre of the page
4. The workflow will automatically revert the deployment on Cybera

#### Method 3: Manual Single Command

Run the following command from the `~/prod/smart-home` directory on the deployment server:

```bash
docker tag smart-home-platform:prev smart-home-platform:active && docker tag smart-home-mysql-container:prev smart-home-mysql-container:active && docker compose -f docker-compose-backend.yml down && docker compose -f docker-compose-backend.yml up -d
```

### Verifying Deployment Status

To check which version is currently deployed, run:

```bash
cd ~/prod/smart-home
./verify-deployment.sh
```

Or manually check with:

```bash
docker compose -f docker-compose-backend.yml images
```

### Rollback Example

A complete log demonstrating the rollback workflow can be found [here](docs/G3/example-reversion.md).

## System description

System description can be downloaded as a pdf file
[here](./docs/TartanSystemDescription.pdf).
https://docs.google.com/document/d/1380wP2QhaNS1oF23kECDXw18lnC4WUw825GCiCAEXec/edit?tab=t.0

## Folder structure

The entire system (Tartan Smart Home Service) resides in *smart-home/*
directory.

Please see the system description (docx) file for more detailed information
about Tartan's design, architecture, requirements, etc.

## Group members (W01)

- Hooriya Kazmi (hkazmi)
- Sergio De Guzman (sdeguzma)
- Rohan Jethwani (rjethwan)
- Alec Zaiane (adzaiane)
- Daniel Thai (dthai)

