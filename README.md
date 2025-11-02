[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/cousPqCv)

# Tartan

## Building

The build instructions can be found [here](./docs/build_instructions.md).

## Deployment and Reversion

- Deployment is handled by `.github/workflows/deploy.yml` and is completely automated
    - When a new commit is pushed to main (via a PR merge), the workflow will build and deploy the new code on Cybera

- Reversion is handled by `revert.yml`
    - To revert to the previous state, navigate to the `Actions` tab of the repository, select `Revert Backend` from the
      left workflow panel, and choose `Run workflow` from the blue box in the centre of the page.
    - A log demonstrating workflow reversion can be found [here](docs/G3/example-reversion.md)
    - If you wanted to manually revert by entering a single command, you could run
      `docker tag smart-home-platform:prev smart-home-platform:active && docker tag smart-home-mysql-container:prev smart-home-mysql-container:active && docker compose -f docker-compose-backend.yml up -d`
      from the `~/prod/smart-home` directory in the deployment server

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

