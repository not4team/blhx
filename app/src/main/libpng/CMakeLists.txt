cmake_minimum_required(VERSION 3.6.0)

set(PNG_SRC
        src/png.c
        src/pngerror.c
        src/pngget.c
        src/pngmem.c
        src/pngpread.c
        src/pngread.c
        src/pngrio.c
        src/pngwio.c
        src/pngrtran.c
        src/pngrutil.c
        src/pngset.c
        src/pngtrans.c
        src/pngwrite.c
        src/pngwtran.c
        src/pngwutil.c
        src/arm/arm_init.c
        src/arm/filter_neon.S
        src/arm/filter_neon_intrinsics.c
        src/arm/palette_neon_intrinsics.c)

find_library(zlib z)

add_library(png SHARED ${PNG_SRC})

target_link_libraries(png ${zlib})