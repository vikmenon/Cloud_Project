============
Sincre v1.0
============
Sincre is a decentralized, loosely-coupled, service-oriented architecture, with a hierarchical layer of inter-operable entities on a large-scale, distributed cloud infrastructure.

-------------------
7 Projects:
-------------------
1. FrameProcessor core (C/C++):
 - Core image/video processing engine
 - Input: frames
 - Output: metadata datasets
 - Algorithm: Scale-invariant feature detection and extraction

2. Sincre Layer-A (C#):
 - Key frame extractor and cloud support services
 - Input: video
 - Output: key frames
 - Algorithm: HSV-based Histogram 

3. Sincre Layer-B (C#):
 - Hosts FrameProcessor core and Sincre Layer-A 
 - Invokes processing pipeline
 - Invokes messaging with search engine
 - Enforces fault-tolerance with search engine
 
4. Sincre Layer-AB WCF Service Library (C#):
 - Edge functions 
 - Admin functions: ETL configuration
 - Layer-AB as a service 
 
5. Sincre Layer-AB Web Service (C#):
 - Library web service
 - Developers API support

7. Sincre WebFront (ASP/C#):
 - User web app

