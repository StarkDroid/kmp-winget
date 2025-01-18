
![Winget Readme Banner](static/kmp-winget-git-banner.png)


# KMP Winget Package Manager (Windows)

A Material theme based package manager crafted for Windows desktop app with the help of Kotlin Multi-Platform and Compose UI, that lets you list, search, upgrade or uninstall packages installed in your system that runs on winget commands in the background. As a bonus, also lets you perform disc cleanup.




## Environment Dependency

To run this project, you will need winget installed in your windows 10 machine.
To know more, follow this [link](https://learn.microsoft.com/en-us/windows/package-manager/winget/)


## Screenshots

<img src="static/Screenshot-1.png" width="500" height="500">
<img src="static/Screenshot-2.png" width="500" height="500">


## Run Locally

It's recommended to use [IntelliJ Idea IDEA](https://www.jetbrains.com/idea/) to run the project

Clone the project

```bash
  git clone https://github.com/kugchennai/kmp-winget
```

Go to the project directory

```bash
  cd kmp-winget
```

Run the app

```bash
  ./gradlew run
```

To build an installable (.msi) package

```bash
  ./gradlew packageDistributionForCurrentOS
```
or you can simply visit the [release]() page to get the latest build


## Features

- Light/dark mode toggle
- List all the installed apps
- Shows installed version of the app
- Shows available updates for an app
- One click solution to disc cleanup
- Search and filter among the list of apps
- Ability to upgrade an app to its available update
- Ability to uninstall an app


## Roadmap

- [ ] Bring support to linux with the help of apt commands
- [ ] Improvements to UI with dialog popup loader
- [ ] Remove MS Store upgrades out of the upgrades list
- [ ] Implement feature to be able to install apps available via winget
- [ ] Do some packages list sanitation

## Tech Stack

**Interface:** Kotlin Multi-Platform, Compose Views, Material3

**Services:** Winget-CLI

**Platform:** Windows

