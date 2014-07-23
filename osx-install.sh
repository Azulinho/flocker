#!/bin/sh

# Get a working version of virtualenv and virtualenvwrapper
curl -O https://glyph.im/pip/bootstrap.sh
chmod u+x ./bootstrap.sh
./bootstrap.sh

# Create a virtualenv, an isolated Python environment, in a new directory
# called "flocker-tutorial":
mkvirtualenv flocker

# Upgrade the pip Python package manager to its latest version inside the
# virtualenv. Some older versions of pip have issues installing Python wheel
# packages.
flocker-tutorial/bin/pip install --upgrade pip

# Install flocker-cli and dependencies inside the virtualenv:
# XXX change to real 0.1.0 URL as part of https://github.com/ClusterHQ/flocker/issues/359:
flocker-tutorial/bin/pip install https://github.com/ClusterHQ/flocker/archive/master.zip
