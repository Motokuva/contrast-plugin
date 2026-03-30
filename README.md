# Contrast Plugin

A RuneLite plugin that adjusts the contrast of the game client using an S-curve filter.

## Features

- Adds a **Contrast** slider (1–200) to the plugin config panel
- Values above 100 increase contrast: dark areas get darker, bright areas get brighter
- Values below 100 flatten the image (reduce contrast)
- Value of 100 is the default — no effect applied

## How it works

The plugin captures each rendered frame and applies a tanh-based S-curve lookup table to all RGB channels before the frame is painted to screen. The lookup table is only rebuilt when the slider value changes, keeping per-frame cost minimal.