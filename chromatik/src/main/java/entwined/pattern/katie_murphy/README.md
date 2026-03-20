# Haven Patterns

Patterns intended for the Haven project, using component tags to target
specific parts of the feature.

## Overview

The goal is to expose OSC triggers for use with the hardware described in the
[Haven OSC Proxy README](https://github.com/FlamingLotusGirls/haven/blob/main/Triggers/OSC_PROXY_README.md).

## Design

Patterns implement a background aesthetic and respond to two categories of triggers:

**Continuous/Discrete triggers** are implemented directly as OSC parameters on
the `LXPattern` itself. The following patterns use this approach:
- `Sunset`
- `HavenFire`
- `HavenSolidsDefault`
Continous/Discrete triggers might be used to change brightness or movement speed of
the pattern of a particular tagged component on the nest. 

**Temporary effects** (On/Off and One-Shot triggers) are implemented as `LXEffect`
subclasses in this directory, so they can be easily copied onto any background
`LXPattern` without modification.
One shot trigger examples might be the spiral chandelier fixture doing a swirl
for a few seconds, or the cockatoo cheeks turning redder and redder the more a 
button is pushed.

## OSC Setup

To wire up a trigger, hover over any OSC control in Chromatik to get its topic
name, then add that topic to the OSC Proxy config in the Haven repo.
