(ns cmr.umm.iso-smap.collection.personnel
  "Contains functions for parsing and generating the ISO SMAP personnel"
  (:require [clojure.data.xml :as x]
            [cmr.common.xml :as cx]
            [cmr.umm.collection :as c]))

(defn- xml-elem->PersonnelRecord
  "Returns a Personnel record from a parsed xml structure"
  [xml-struct]
  (let [person-name (cx/string-at-path xml-struct [:individualName
                                                   :CharacterString])
        org-name (cx/string-at-path xml-struct [:organisationName
                                                :CharacterString])
        contact-name (or person-name org-name)
        email-str (cx/string-at-path xml-struct [:contactInfo
                                                 :address
                                                 :CI_Address
                                                 :electronicMailAddress
                                                 :CharacterString])
        email (when email-str
                [(c/map->Contact {:type :email
                                  :value email-str})])
        role (cx/string-at-path xml-struct [:role
                                            :CI_RoleCode])]
    (when contact-name
      (c/map->Personnel {:roles [role]
                         :last-name contact-name
                         :contacts email}))))

(defn xml-elem->personnel
  "Returns the personnel field of a UMM Collection from a parsed XML structure"
  [xml-struct]
  (let [contact-elements (remove nil?
                                 (flatten
                                   (vector
                                     (cx/elements-at-path
                                       xml-struct
                                       [:seriesMetadata
                                        :MI_Metadata
                                        :contact
                                        :CI_ResponsibleParty])
                                     (cx/elements-at-path
                                       xml-struct
                                       [:seriesMetadata
                                        :MI_Metadata
                                        :identificationInfo
                                        :MD_DataIdentification
                                        :citation
                                        :CI_Citation
                                        :citedResponsibleParty
                                        :CI_ResponsibleParty])
                                     (cx/elements-at-path
                                       xml-struct
                                       [:seriesMetadata
                                        :MI_Metadata
                                        :identificationInfo
                                        :MD_DataIdentification
                                        :pointOfContact
                                        :CI_ResponsibleParty])
                                     (cx/elements-at-path
                                       xml-struct
                                       [:seriesMetadata
                                        :MI_Metadata
                                        :distributionInfo
                                        :MD_Distribution
                                        :distributor
                                        :MD_Distributor
                                        :distributorContact
                                        :CI_ResponsibleParty]))))]
    (not-empty (remove nil?
                       (map xml-elem->PersonnelRecord contact-elements)))))
