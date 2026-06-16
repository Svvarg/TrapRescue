.PHONY: build test build-fast clean echo
include gradle.properties

build:
	gradle build

build-fast:
	gradle build -x test

test:
	gradle test --offline

LOCAL_DST=/v/tsrv/raw-kcauldron/mods/

deploy-to-local:
ifndef mod_id
	$(error mod_id not defined or empty)
endif
	rm $(LOCAL_DST)$(mod_id)-*.jar || true
	cp build/libs/$(mod_id)-$(version).jar $(LOCAL_DST)

clean:
	rm -r build/*

echo:
	@echo "mod_id: $(mod_id) version: $(version)"

