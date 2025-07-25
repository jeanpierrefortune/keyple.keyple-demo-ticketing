#!/bin/sh

dir=$1
tag=$2
echo "Input dir: '$dir'"
echo "Input tag: '$tag'"

version=`grep "^version" $dir/gradle.properties | cut -d= -f2 | tr -d "[:space:]"`
echo "Version in 'gradle.properties' file: '$version'"

if [ "$tag" != "" ]; then
  echo "Release mode: check version consistency..."
  if [ "$tag" != "$version" ]; then
    echo "ERROR: the tag '$tag' is different from the version '$version' in the 'gradle.properties' file"
    exit 1
  fi
else
  echo "Snapshot mode: fetch existing tags..."
  git fetch --tags
  if [ $(git tag -l "$version") ]; then
    echo "ERROR: version '$version' has already been released"
    exit 1
  fi
fi
