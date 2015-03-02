(ns cmr.umm.test.generators.granule
  "Provides clojure.test.check generators for use in testing other projects."
  (:require [clojure.test.check.generators :as gen]
            [cmr.common.test.test-check-ext :as ext-gen :refer [optional]]
            [cmr.umm.test.generators.collection :as c]
            [cmr.umm.test.generators.granule.temporal :as gt]
            [cmr.umm.test.generators.collection.product-specific-attribute :as psa]
            [cmr.umm.granule :as g]
            [cmr.umm.test.generators.spatial :as spatial-gen]))

;;; granule related
(def granule-urs
  (ext-gen/string-ascii 1 10))

(def coll-refs-w-entry-title
  (ext-gen/model-gen g/collection-ref c/entry-titles))

(def coll-refs-w-short-name-version
  (ext-gen/model-gen g/collection-ref c/short-names c/version-ids))

(def coll-refs
  (gen/one-of [coll-refs-w-entry-title coll-refs-w-short-name-version]))

(def product-specific-attribute-refs
  (ext-gen/model-gen g/->ProductSpecificAttributeRef psa/names (gen/vector psa/string-values 1 3)))

(def data-granules
  (ext-gen/model-gen
    g/map->DataGranule
    (gen/hash-map :producer-gran-id (ext-gen/optional (ext-gen/string-ascii 1 10))
                  :day-night (gen/elements ["DAY" "NIGHT" "BOTH" "UNSPECIFIED"])
                  :production-date-time ext-gen/date-time
                  :size (ext-gen/choose-double 0 1024))))

(def cloud-cover-values
  (gen/fmap double gen/ratio))

(def characteristic-ref-names
  (ext-gen/string-ascii 1 10))

(def characteristic-ref-values
  (ext-gen/string-ascii 1 10))

(def characteristic-refs
  (ext-gen/model-gen g/->CharacteristicRef characteristic-ref-names characteristic-ref-values))

(def sensor-ref-short-names
  (ext-gen/string-ascii 1 10))

(def sensor-refs
  (ext-gen/model-gen g/->SensorRef
                     sensor-ref-short-names
                     (ext-gen/nil-if-empty (gen/vector characteristic-refs 0 4))))

(def instrument-ref-short-names
  (ext-gen/string-ascii 1 10))

(def operation-modes
  (ext-gen/string-ascii 1 10))

(def instrument-refs
  (ext-gen/model-gen g/->InstrumentRef
                     instrument-ref-short-names
                     (ext-gen/nil-if-empty (gen/vector characteristic-refs 0 4))
                     (ext-gen/nil-if-empty (gen/vector sensor-refs 0 4))
                     (ext-gen/nil-if-empty (gen/vector operation-modes 0 4))))

(def platform-ref-short-names
  (ext-gen/string-ascii 1 10))

(def platform-refs
  (ext-gen/model-gen g/->PlatformRef
                     platform-ref-short-names
                     (ext-gen/nil-if-empty (gen/vector instrument-refs 0 4))))

(def two-d-coordinate-system
  (let [coords-gen (gen/fmap sort (gen/vector (ext-gen/choose-double 0 1000) 1 2))]
    (gen/fmap
      (fn [[name [start-coordinate-1 end-coordinate-1] [start-coordinate-2 end-coordinate-2]]]
        (g/map->TwoDCoordinateSystem {:name name
                                      :start-coordinate-1 start-coordinate-1
                                      :end-coordinate-1 end-coordinate-1
                                      :start-coordinate-2 start-coordinate-2
                                      :end-coordinate-2 end-coordinate-2}))
      (gen/tuple (ext-gen/string-ascii 1 10)
                 coords-gen
                 coords-gen))))

(def spatial-coverages
  (ext-gen/model-gen
    g/map->SpatialCoverage
    (gen/one-of
      [(gen/hash-map :geometries (gen/vector spatial-gen/geometries 1 5))
       (gen/hash-map :orbit spatial-gen/orbits)])))

(def granules
  (ext-gen/model-gen
    g/map->UmmGranule
    (gen/hash-map
      :granule-ur granule-urs
      :data-provider-timestamps c/data-provider-timestamps
      :collection-ref coll-refs
      :data-granule (ext-gen/optional data-granules)
      :access-value (ext-gen/optional (ext-gen/choose-double -10 10))
      :temporal gt/temporal
      :orbit-calculated-spatial-domains (ext-gen/nil-if-empty
                                          (gen/vector
                                            spatial-gen/orbit-calculated-spatial-domains 0 5))
      :platform-refs (ext-gen/nil-if-empty (gen/vector platform-refs 0 4))
      :project-refs (ext-gen/nil-if-empty (gen/vector (ext-gen/string-ascii 1 10) 0 3))
      :cloud-cover (ext-gen/optional cloud-cover-values)
      :two-d-coordinate-system (ext-gen/optional two-d-coordinate-system)
      :related-urls (ext-gen/nil-if-empty (gen/vector c/related-url 0 5))
      :spatial-coverage (ext-gen/optional spatial-coverages)
      :product-specific-attributes (ext-gen/nil-if-empty
                                     (gen/vector product-specific-attribute-refs 0 5)))))
