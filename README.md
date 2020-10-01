# Fold & Cut Implementation by David Benjamin and Anthony Lee

This is a Java implementation of the [straight-skeleton method for the
fold-and-cut problem](http://erikdemaine.org/foldcut/#skeleton),
written by David Benjamin and Anthony Lee as a final project for
[6.849: Geometric Folding Algorithms](http://courses.csail.mit.edu/6.849/)
in [Fall 2010](http://courses.csail.mit.edu/6.849/fall10/).

## campskeleton

This implementation uses (an old version of)
[campskeleton](https://github.com/twak/campskeleton/) to compute the
straight skeleton.  Specifically, `src/straightskeleton` and `src/utils`
were obtained from and modified from the campskeleton project.
Both campskeleton and this codebase are
[licensed under the Apache License 2.0](LICENSE).
